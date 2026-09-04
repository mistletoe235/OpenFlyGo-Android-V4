package edu.playground.djivln.mini2;

import android.app.Activity;

import androidx.annotation.Nullable;

import dji.common.error.DJIError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.useraccount.UserAccountInformation;
import dji.sdk.useraccount.UserAccountManager;

/** MSDK4 equivalent of the V5 account controller. */
final class DjiUserAccountController implements AutoCloseable {
    interface Listener {
        void onChanged(Snapshot snapshot);
        void onLoginResult(boolean success, @Nullable String error);
    }

    static final class Snapshot {
        final UserAccountState state;
        @Nullable final String maskedAccount;
        @Nullable final String lastError;

        Snapshot(UserAccountState state, @Nullable String maskedAccount, @Nullable String lastError) {
            this.state = state == null ? UserAccountState.UNKNOWN : state;
            this.maskedAccount = maskedAccount;
            this.lastError = lastError;
        }

        boolean isLoggedIn() {
            return DjiAccountStatePolicy.isLoggedIn(state.name());
        }

        boolean shouldOfferLogin() {
            return DjiAccountStatePolicy.shouldOfferLogin(state.name());
        }
    }

    private final Activity activity;
    private final Listener listener;
    private final UserAccountManager manager = UserAccountManager.getInstance();
    private final UserAccountManager.UserAccountStateChangeListener stateListener =
            this::onSdkStateChanged;
    private int refreshGeneration;
    private boolean closed;
    private Snapshot current = new Snapshot(UserAccountState.UNKNOWN, null, null);

    DjiUserAccountController(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
        manager.addUserAccountStateChangeListener(stateListener);
        refresh();
    }

    Snapshot snapshot() {
        return current;
    }

    void refresh() {
        if (closed) return;
        UserAccountState state = manager.getUserAccountState();
        int generation = ++refreshGeneration;
        if (!isLoggedIn(state)) {
            publish(new Snapshot(state, null, null));
            return;
        }
        publish(new Snapshot(state, current.maskedAccount, null));
        manager.getLoggedInDJIUserAccountName(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override public void onSuccess(String account) {
                activity.runOnUiThread(() -> {
                    if (!closed && generation == refreshGeneration) {
                        publish(new Snapshot(state, maskAccount(account), null));
                    }
                });
            }

            @Override public void onFailure(DJIError error) {
                activity.runOnUiThread(() -> {
                    if (!closed && generation == refreshGeneration) {
                        publish(new Snapshot(state, null, detail(error)));
                    }
                });
            }
        });
    }

    void login() {
        if (closed) return;
        manager.logIntoDJIUserAccount(
                activity,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override public void onSuccess(UserAccountState state) {
                        activity.runOnUiThread(() -> {
                            if (closed) return;
                            refresh();
                            boolean loggedIn = isLoggedIn(state)
                                    || isLoggedIn(manager.getUserAccountState());
                            listener.onLoginResult(loggedIn,
                                    loggedIn ? null : activity.getString(
                                            R.string.dji_account_login_unconfirmed));
                        });
                    }

                    @Override public void onFailure(DJIError error) {
                        String detail = detail(error);
                        activity.runOnUiThread(() -> {
                            if (closed) return;
                            publish(new Snapshot(manager.getUserAccountState(),
                                    current.maskedAccount, detail));
                            listener.onLoginResult(false, detail);
                        });
                    }
                },
                false
        );
    }

    private void onSdkStateChanged(UserAccountState state, UserAccountInformation information) {
        activity.runOnUiThread(() -> {
            if (closed) return;
            ++refreshGeneration;
            publish(new Snapshot(
                    state,
                    isLoggedIn(state) && information != null
                            ? maskAccount(information.getAccount()) : null,
                    null
            ));
        });
    }

    private void publish(Snapshot snapshot) {
        current = snapshot;
        listener.onChanged(snapshot);
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        ++refreshGeneration;
        manager.removeUserAccountStateChangeListener(stateListener);
    }

    static boolean isLoggedIn(@Nullable UserAccountState state) {
        return state != null && DjiAccountStatePolicy.isLoggedIn(state.name());
    }

    @Nullable private static String maskAccount(@Nullable String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.isEmpty()) return null;
        if (value.length() <= 3) return "***";
        int at = value.indexOf('@');
        if (at >= 0) return value.substring(0, Math.min(2, at)) + "***" + value.substring(at);
        return value.substring(0, 3) + "***" + value.substring(value.length() - 2);
    }

    private static String detail(@Nullable DJIError error) {
        if (error == null) return "unknown";
        String description = error.getDescription();
        return description == null || description.trim().isEmpty()
                ? error.toString() : description.trim();
    }
}

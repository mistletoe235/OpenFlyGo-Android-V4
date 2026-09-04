package edu.playground.djivln.mini2;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/** Keeps the in-app picker and Android's per-app locale setting in sync. */
final class AppLanguageController {
    private static final String PREFS = "openfly_language";
    private static final String SELECTION_COMPLETED = "selection_completed";
    private static final String TAG_ZH_HANS = "zh-Hans";
    private static final String TAG_EN = "en";

    private AppLanguageController() {}

    static void maybeShowFirstLaunch(AppCompatActivity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!preferences.getBoolean(SELECTION_COMPLETED, false)) {
            showPicker(activity, true, true);
        }
    }

    static void showSettingsPicker(AppCompatActivity activity, boolean canChangeNow) {
        if (!canChangeNow) {
            Toast.makeText(activity, R.string.language_change_blocked, Toast.LENGTH_LONG).show();
            return;
        }
        showPicker(activity, false, true);
    }

    private static void showPicker(AppCompatActivity activity, boolean firstLaunch, boolean canChangeNow) {
        if (!canChangeNow || activity.isFinishing() || activity.isDestroyed()) return;
        String[] labels = {
                activity.getString(R.string.language_simplified_chinese),
                activity.getString(R.string.language_english),
        };
        String current = currentTag();
        int[] selected = {current.startsWith("zh") ? 0 : 1};
        float density = activity.getResources().getDisplayMetrics().density;
        int horizontalPadding = Math.round(24 * density);
        int itemPadding = Math.round(4 * density);
        int bottomPadding = Math.round(8 * density);
        RadioGroup choices = new RadioGroup(activity);
        choices.setOrientation(RadioGroup.VERTICAL);
        for (int index = 0; index < labels.length; index++) {
            final int choiceIndex = index;
            RadioButton choice = new RadioButton(activity);
            choice.setId(View.generateViewId());
            choice.setText(labels[index]);
            choice.setChecked(index == selected[0]);
            choice.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);
            choice.setOnCheckedChangeListener((button, checked) -> {
                if (checked) selected[0] = choiceIndex;
            });
            choices.addView(choice);
        }
        TextView note = new TextView(activity);
        note.setText(R.string.language_change_later);
        note.setPadding(0, 0, 0, bottomPadding);
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(horizontalPadding, 0, horizontalPadding, bottomPadding);
        content.addView(note);
        content.addView(choices);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.choose_language)
                .setView(content)
                .setPositiveButton(R.string.action_continue, null)
                .setCancelable(!firstLaunch);
        if (!firstLaunch) builder.setNegativeButton(R.string.action_cancel, null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String tag = selected[0] == 0 ? TAG_ZH_HANS : TAG_EN;
            activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putBoolean(SELECTION_COMPLETED, true)
                    .apply();
            dialog.dismiss();
            if (!currentTag().equalsIgnoreCase(tag)) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
            }
        }));
        dialog.show();
    }

    private static String currentTag() {
        Locale appLocale = AppCompatDelegate.getApplicationLocales().get(0);
        if (appLocale != null) return appLocale.toLanguageTag();
        String systemLanguage = Locale.getDefault().getLanguage();
        return systemLanguage != null && systemLanguage.startsWith("zh") ? TAG_ZH_HANS : TAG_EN;
    }
}

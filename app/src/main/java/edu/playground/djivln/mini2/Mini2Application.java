package edu.playground.djivln.mini2;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public final class Mini2Application extends Application {
    // Session-only HUD preferences. They intentionally live in the Application instead of
    // SharedPreferences: Activity recreation and page changes keep them, a real app restart does not.
    private boolean vlnPanelMinimized = true;
    private boolean flightPanelMinimized;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Helper.install(this);
    }

    public boolean isVlnPanelMinimized() {
        return vlnPanelMinimized;
    }

    public void setVlnPanelMinimized(boolean minimized) {
        vlnPanelMinimized = minimized;
    }

    public boolean isFlightPanelMinimized() {
        return flightPanelMinimized;
    }

    public void setFlightPanelMinimized(boolean minimized) {
        flightPanelMinimized = minimized;
    }

    public void resetHudSessionState() {
        vlnPanelMinimized = true;
        flightPanelMinimized = false;
    }
}

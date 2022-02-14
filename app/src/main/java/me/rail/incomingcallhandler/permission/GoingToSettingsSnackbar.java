package me.rail.incomingcallhandler.permission;

import android.app.Activity;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class GoingToSettingsSnackbar {
    private final Activity activity;
    private final View view;

    public GoingToSettingsSnackbar(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    public void showSnackbar(String text, String actionName) {
        Snackbar goingToSettingsSnackbar = createSnackbar(text);
        goingToSettingsSnackbar = setAction(goingToSettingsSnackbar, actionName);
        goingToSettingsSnackbar.show();
    }

    private Snackbar createSnackbar(String text) {
        return Snackbar.make(
                view,
                text,
                Snackbar.LENGTH_LONG
        );
    }

    private Snackbar setAction(Snackbar snackbar, String actionName) {
        return snackbar.setAction(actionName, view1 ->
                SettingsOpener.openSettings(activity, Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
    }
}

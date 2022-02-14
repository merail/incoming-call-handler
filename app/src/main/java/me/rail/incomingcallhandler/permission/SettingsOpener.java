package me.rail.incomingcallhandler.permission;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class SettingsOpener {
    private static final String scheme = "package";

    public static void openSettings(Activity activity, String action) {
        Intent intent = new Intent(action,
                Uri.fromParts(scheme, activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}

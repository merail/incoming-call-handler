package me.rail.incomingcallhandler;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import kotlin.Pair;
import kotlin.Unit;

import merail.tools.permissions.SettingsSnackbar;
import merail.tools.permissions.runtime.RuntimePermissionRequester;
import merail.tools.permissions.runtime.RuntimePermissionState;
import merail.tools.permissions.special.SpecialPermissionRequester;

public class MainActivity extends AppCompatActivity {

    private SpecialPermissionRequester specialPermissionRequester;
    private RuntimePermissionRequester runtimePermissionRequester;

    private final String[] runtimePermissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
    };

    private Button getRuntimePermissions;
    private Button getSpecialPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        specialPermissionRequester = new SpecialPermissionRequester(this, Manifest.permission.SYSTEM_ALERT_WINDOW);
        runtimePermissionRequester = new RuntimePermissionRequester(this, runtimePermissions);

        getRuntimePermissions = findViewById(R.id.requestRuntimePermission);
        setOnGetPermissionsClickListener();
        getSpecialPermissions = findViewById(R.id.requestSpecialPermission);
        setOnGetSpecialPermissionsClickListener();

        if (!runtimePermissionRequester.areAllPermissionsGranted()) {
            runtimePermissionRequester.requestPermissions(this::onRuntimePermissionsRequestResult);
        }
        if (!specialPermissionRequester.isPermissionGranted()) {
            specialPermissionRequester.requestPermission(this::onSpecialPermissionsRequestResult);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setGetSpecialPermissionsVisibility(specialPermissionRequester.isPermissionGranted());
        setGetRuntimePermissionsVisibility(runtimePermissionRequester.areAllPermissionsGranted());
    }

    private void setOnGetPermissionsClickListener() {
        getRuntimePermissions.setOnClickListener(view -> runtimePermissionRequester.requestPermissions(this::onRuntimePermissionsRequestResult));
    }

    private Unit onRuntimePermissionsRequestResult(Map<String, ? extends RuntimePermissionState> permissionsStateMap) {
        setGetRuntimePermissionsVisibility(runtimePermissionRequester.areAllPermissionsGranted());
        if (permissionsStateMap.containsValue(RuntimePermissionState.PERMANENTLY_DENIED)) {
            SettingsSnackbar settingsOpeningSnackbar = new SettingsSnackbar(
                    this,
                    getWindow().getDecorView()
            );
            settingsOpeningSnackbar.showSnackbar("You must grant permissions in Settings!", "Settings");
        }
        return Unit.INSTANCE;
    }

    private Unit onSpecialPermissionsRequestResult(Pair<String, Boolean> permissionStatePair) {
        setGetSpecialPermissionsVisibility(specialPermissionRequester.isPermissionGranted());
        return Unit.INSTANCE;
    }

    private void setOnGetSpecialPermissionsClickListener() {
        getSpecialPermissions.setOnClickListener(view -> specialPermissionRequester.requestPermission(this::onSpecialPermissionsRequestResult));
    }

    private void setGetSpecialPermissionsVisibility(Boolean hide) {
        if (hide) {
            getSpecialPermissions.setVisibility(View.GONE);
        } else {
            getSpecialPermissions.setVisibility(View.VISIBLE);
        }
    }

    private void setGetRuntimePermissionsVisibility(Boolean hide) {
        if (hide) {
            getRuntimePermissions.setVisibility(View.GONE);
        } else {
            getRuntimePermissions.setVisibility(View.VISIBLE);
        }
    }
}
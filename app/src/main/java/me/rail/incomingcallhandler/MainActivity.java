package me.rail.incomingcallhandler;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import me.rail.incomingcallhandler.permission.GoingToSettingsSnackbar;
import me.rail.incomingcallhandler.permission.RuntimePermissionRequester;
import me.rail.incomingcallhandler.permission.SpecialPermissionRequester;

public class MainActivity extends AppCompatActivity {

    private SpecialPermissionRequester specialPermissionRequester;
    private RuntimePermissionRequester runtimePermissionRequester;

    private final String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
    };
    private ArrayList<String> notGrantedPermissions;

    private Button getRuntimePermissions;
    private Button getSpecialPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        specialPermissionRequester = new SpecialPermissionRequester(this);
        runtimePermissionRequester = new RuntimePermissionRequester(this);

        getRuntimePermissions = findViewById(R.id.requestRuntimePermission);
        setOnGetPermissionsClickListener();
        getSpecialPermissions = findViewById(R.id.requestSpecialPermission);
        setOnGetSpecialPermissionsClickListener();

        if (!specialPermissionRequester.checkSystemAlertWindowPermission()) {
            specialPermissionRequester.requestSystemAlertWindowPermission();
        }
        if (!runtimePermissionRequester.checkSelfPermissions(permissions)) {
            runtimePermissionRequester.requestPermissions();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkSpecialPermissions();
        checkRuntimePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        notGrantedPermissions =
                runtimePermissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);

        setGetRuntimePermissionsVisibility(notGrantedPermissions.isEmpty());
    }

    private void setOnGetPermissionsClickListener() {
        getRuntimePermissions.setOnClickListener(view -> {
            checkPermissionsForRationale();
            checkDeniedPermissions(view);
        });
    }

    private void setOnGetSpecialPermissionsClickListener() {
        getSpecialPermissions.setOnClickListener(view ->
                specialPermissionRequester.requestSystemAlertWindowPermission());
    }

    private void checkSpecialPermissions() {
        Boolean isSpecialPermissionGranted = specialPermissionRequester.checkSystemAlertWindowPermission();
        setGetSpecialPermissionsVisibility(isSpecialPermissionGranted);
    }

    private void checkRuntimePermissions() {
        Boolean isAllRuntimePermissionsGranted = runtimePermissionRequester.checkSelfPermissions(permissions);
        setGetRuntimePermissionsVisibility(isAllRuntimePermissionsGranted);
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

    private void checkPermissionsForRationale() {
        ArrayList<String> permissionsForRationale = runtimePermissionRequester.getPermissionsForRationale(notGrantedPermissions);
        if (!permissionsForRationale.isEmpty()) {
            runtimePermissionRequester.setPermissionsForRequest(permissionsForRationale);
            runtimePermissionRequester.requestPermissions();
        }
    }

    private void checkDeniedPermissions(View view) {
        ArrayList<String> deniedPermissions = runtimePermissionRequester.getDeniedPermissions(notGrantedPermissions);
        if (!deniedPermissions.isEmpty()) {
            GoingToSettingsSnackbar goingToSettingsSnackbar = new GoingToSettingsSnackbar(this, view);
            goingToSettingsSnackbar.showSnackbar("You must grant permissions in Settings!", "Settings");
        }
    }
}
package me.rail.incomingcallhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class IncomingCallBroadcastReceiver extends BroadcastReceiver {
    private static WindowManager windowManager;
    private static ViewGroup windowLayout;

    private WindowManager.LayoutParams params;
    private float x;
    private float y;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (number != null) {
                if (intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                        .equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    showWindow(context, number);
                } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                        .equals(TelephonyManager.EXTRA_STATE_IDLE) ||
                        intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                        .equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    closeWindow();
                }
            }
        }
    }

    private void showWindow(final Context context, String phone) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowLayout = (ViewGroup) View.inflate(context, R.layout.window_call_info, null);
        getLayoutParams();
        setOnTouchListener();

        TextView numberTextView = windowLayout.findViewById(R.id.number);
        numberTextView.setText(phone);
        Button cancelButton = windowLayout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(view -> closeWindow());

        windowManager.addView(windowLayout, params);
    }

    private void setOnTouchListener() {
        windowLayout.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getRawX();
                    y = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    params.x = params.x - (int) (x - event.getRawX());
                    params.y = params.y - (int) (y - event.getRawY());
                    windowManager.updateViewLayout(windowLayout, params);
                    x = event.getRawX();
                    y = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                default:
                    break;
            }
            return false;
        });
    }

    private void closeWindow() {
        if (windowLayout != null) {
            windowManager.removeView(windowLayout);
            windowLayout = null;
        }
    }

    private int getWindowsTypeParameter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void getLayoutParams() {
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getWindowsTypeParameter(),
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        params.format = 1;
        params.width = getWindowWidth();
    }

    private int getWindowWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return (int) (0.939 * (double) metrics.widthPixels);
    }
}

package merail.calls.handler

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView

internal const val INCOMING_CALL_END_ACTION_NAME = "ACTION_INCOMING_CALL_END"

class IncomingCallBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private var windowManager: WindowManager? = null

        @SuppressLint("StaticFieldLeak")
        private var windowLayout: ViewGroup? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (phoneNumber != null) {
                    if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING) {
                        showWindow(context, phoneNumber)
                    }
                    if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE ||
                        intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                        closeWindow()
                    }
                }
            } else {
                if (intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE ||
                    intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    val incomingCallEndIntent = Intent(context, IncomingCallService::class.java).apply {
                        action = INCOMING_CALL_END_ACTION_NAME
                    }
                    context.applicationContext.startService(incomingCallEndIntent)
                }
            }
        }
    }

    private fun showWindow(context: Context, phone: String) {
        if (windowLayout == null) {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowLayout = View.inflate(context, R.layout.window_call_info, null) as ViewGroup?
            windowLayout?.let {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    },
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT,
                )

                params.gravity = Gravity.CENTER
                params.format = 1

                val metrics = DisplayMetrics()
                windowManager?.defaultDisplay?.getMetrics(metrics)
                params.width = (0.8 * metrics.widthPixels.toDouble()).toInt()

                val numberTextView = it.findViewById<TextView>(R.id.number)
                numberTextView.text = phone

                windowManager?.addView(it, params)
            }
        }
    }

    private fun closeWindow() {
        if (windowLayout != null) {
            windowManager?.removeView(windowLayout)
            windowManager = null
            windowLayout = null
        }
    }
}

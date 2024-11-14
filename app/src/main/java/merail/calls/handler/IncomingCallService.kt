package merail.calls.handler

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class IncomingCallService : CallScreeningService() {

    companion object {
        private var windowManager: WindowManager? = null

        @SuppressLint("StaticFieldLeak")
        private var windowLayout: ViewGroup? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == INCOMING_CALL_END_ACTION_NAME) {
            closeWindow()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
                val phoneNumber = callDetails.handle.schemeSpecificPart
                phoneNumber?.let {
                    showWindow(this, it)
                }
            }
        }
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun showWindow(context: Context, phone: String) {
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

            params.width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager?.currentWindowMetrics
                val insets = windowMetrics?.getWindowInsets()?.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                if (windowMetrics != null && insets != null) {
                    (0.8 * (windowMetrics.bounds.width() - insets.left - insets.right)).toInt()
                } else {
                    params.width
                }
            } else {
                val metrics = DisplayMetrics()
                windowManager?.defaultDisplay?.getMetrics(metrics)
                (0.8 * metrics.widthPixels.toDouble()).toInt()
            }

            val numberTextView = it.findViewById<TextView>(R.id.number)
            numberTextView.text = phone

            windowManager?.addView(it, params)
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
package merail.calls.handler

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView


class IncomingCallBroadcastReceiver : BroadcastReceiver() {

    companion object {

        private const val WINDOW_WIDTH_RATIO = 0.8f

        private lateinit var windowManager: WindowManager

        @SuppressLint("StaticFieldLeak")
        private var windowLayout: ViewGroup? = null
    }

    private var params = WindowManager.LayoutParams(
        // width
        WindowManager.LayoutParams.WRAP_CONTENT,
        // height
        WindowManager.LayoutParams.WRAP_CONTENT,
        // type
        windowType,
        // flags
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        // format
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.CENTER
        format = 1
    }

    private var x = 0f

    private var y = 0f

    private val Intent.needToShowWindow: Boolean
        get() = getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING

    private val Intent.needToCloseWindow: Boolean
        get() = getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE ||
                getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK

    private val windowType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private val WindowManager.windowWidth: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = currentWindowMetrics
            val insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            (WINDOW_WIDTH_RATIO * (windowMetrics.bounds.width() - insets.left - insets.right)).toInt()
        } else {
            DisplayMetrics().apply {
                defaultDisplay?.getMetrics(this)
            }.run {
                (WINDOW_WIDTH_RATIO * widthPixels).toInt()
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            when {
                phoneNumber == null -> Unit
                intent.needToShowWindow -> showWindow(context, phoneNumber)
                intent.needToCloseWindow -> closeWindow()
            }
        }
    }

    private fun showWindow(context: Context, phone: String) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowLayout = View.inflate(context, R.layout.window_call_info, null) as ViewGroup?
        windowLayout?.let {
            params.width = windowManager.windowWidth
            val numberTextView = it.findViewById<TextView>(R.id.number)
            numberTextView.text = phone
            val cancelButton = it.findViewById<Button>(R.id.cancel)
            cancelButton.setOnClickListener {
                closeWindow()
            }
            windowManager.addView(it, params)
            setOnTouchListener()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener() {
        windowLayout?.setOnTouchListener { view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                }
                MotionEvent.ACTION_MOVE -> updateWindowLayoutParams(event)
                MotionEvent.ACTION_UP -> view.performClick()
                else -> Unit
            }
            false
        }
    }

    private fun updateWindowLayoutParams(event: MotionEvent) {
        params.x -= (x - event.rawX).toInt()
        params.y -= (y - event.rawY).toInt()
        windowManager.updateViewLayout(windowLayout, params)
        x = event.rawX
        y = event.rawY
    }

    private fun closeWindow() {
        if (windowLayout != null) {
            windowManager.removeView(windowLayout)
            windowLayout = null
        }
    }
}

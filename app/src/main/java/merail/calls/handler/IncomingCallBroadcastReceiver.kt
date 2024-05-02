package merail.calls.handler

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager


class IncomingCallBroadcastReceiver : BroadcastReceiver() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val incomingCallAlert = IncomingCallAlert()
    }

    private val Intent.needToShowWindow: Boolean
        get() = getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING

    private val Intent.needToCloseWindow: Boolean
        get() = getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_IDLE ||
                getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_OFFHOOK

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                when {
                    phoneNumber == null -> Unit
                    intent.needToShowWindow -> incomingCallAlert.showWindow(context, phoneNumber)
                    intent.needToCloseWindow -> incomingCallAlert.closeWindow()
                }
            }
        }
    }
}

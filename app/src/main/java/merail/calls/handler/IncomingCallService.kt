package merail.calls.handler

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.N)
class IncomingCallService : CallScreeningService() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val incomingCallAlert = IncomingCallAlert()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        incomingCallAlert.closeWindow()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
                val phoneNumber = callDetails.handle.schemeSpecificPart
                phoneNumber?.let {
                    incomingCallAlert.showWindow(this, it)
                }
            }
            respondToCall(callDetails, CallResponse.Builder().build())
        }
    }
}
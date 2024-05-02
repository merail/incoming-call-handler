package merail.calls.handler

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.N)
class IncomingCallService : CallScreeningService() {

    private val incomingCallAlert = IncomingCallAlert()

    override fun onScreenCall(callDetails: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
                val phoneNumber = callDetails.handle.schemeSpecificPart
                when {
                    phoneNumber == null -> Unit
                    else -> incomingCallAlert.showWindow(this, callDetails.handle.schemeSpecificPart)
                }
            }
            val callResponse = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }
    }
}
package merail.calls.handler

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import org.json.JSONObject


@RequiresApi(Build.VERSION_CODES.N)
class IncomingCallService : CallScreeningService() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val incomingCallAlert = IncomingCallAlert()

        private var numbersList = arrayOf("abcde", "blabla");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        incomingCallAlert.closeWindow()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        System.out.println("onscreenscall");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val stringBuilder = StringBuilder()
            try {
                applicationContext.openFileInput("numbers_list").use {
                    val timeStart = System.currentTimeMillis();
                    val myString: String = IOUtils.toString(it, "UTF-8");
                    val jsonObj = JSONObject(myString);
                    val numbersArray = jsonObj.getJSONArray("numbers")
                    val tm = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                    val countryCodeValue = tm.networkCountryIso
                    System.out.println("country code is " + countryCodeValue)

                    System.out.println(callDetails.gatewayInfo);
                    System.out.println(callDetails.handle.toString());
                    System.out.println(callDetails.handle.schemeSpecificPart.toString() +" is calling " + numbersArray.length() + " loaded in " + (System.currentTimeMillis() - timeStart).toString())

                    if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
                        val phoneNumber = callDetails.handle.schemeSpecificPart
                        phoneNumber?.let {
                            incomingCallAlert.showWindow(this, it)
                        }
                        OperationLogger().saveToLog(applicationContext, "Incoming call from " + phoneNumber);
//                    val response = CallResponse.Builder()
//                        // Sets whether the incoming call should be blocked.
//                        .setDisallowCall(true)
//                        // Sets whether the incoming call should be rejected as if the user did so manually.
//                        .setRejectCall(true)
//                        // Sets whether ringing should be silenced for the incoming call.
//                        .setSilenceCall(true)
//                        // Sets whether the incoming call should not be displayed in the call log.
//                        .setSkipCallLog(false)
//                        // Sets whether a missed call notification should not be shown for the incoming call.
//                        .setSkipNotification(false)
//                        .build()
//
//                    respondToCall(callDetails, response)

                    } else {
                        System.out.println("here, right?")

                        System.out.println("dupax " + numbersList.size)
                        numbersList = arrayOf("keke");
//                    System.out.println("numbersList " + Arrays.toString(numbersList) + " " + numbersList.size);
//                    val response = CallResponse.Builder()
//                        // Sets whether the incoming call should be blocked.
//                        .setDisallowCall(true)
//                        .setRejectCall(true)
//                        .build()
//
//                    respondToCall(callDetails, response)
                    }
//                respondToCall(callDetails, CallResponse.Builder().build())
                }
            } catch (e: Exception) {

            }
        }
    }
}
package merail.calls.handler

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject


@RequiresApi(Build.VERSION_CODES.N)
class IncomingCallService : CallScreeningService() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val incomingCallAlert = IncomingCallAlert()

        private val countryCodesHelper = CountryCodes();

        private lateinit var numbersDb: JSONArray;

        private var loadedDbVersion = "";

        private val preferenceHelper = PreferenceHelper();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        incomingCallAlert.closeWindow()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING || true) {
            val phoneNumber = callDetails.handle.schemeSpecificPart
            phoneNumber?.let {
                incomingCallAlert.showWindow(this, it)
            }

            val savedDbVersion = preferenceHelper.getPreference(applicationContext, "saved_db_timestamp_pref");

            if (loadedDbVersion !== savedDbVersion) {
                    //                Let's load the DB first
                try {
                    applicationContext.openFileInput("numbers_list").use {
                        loadedDbVersion = savedDbVersion!!;
                        val myString: String = IOUtils.toString(it, "UTF-8");
                        val jsonObj = JSONObject(myString);
                        val numbersArray = jsonObj.getJSONArray("numbers")
                        numbersDb = numbersArray
                        val countryCodeValue = (this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso
                        val countryPrefix = "+" + countryCodesHelper.getCode(countryCodeValue.uppercase())
                    }
                } catch (e: Exception) {

                }
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

//                        Handle in default dialer
            respondToCall(callDetails, CallResponse.Builder().build())

        }
    }
}
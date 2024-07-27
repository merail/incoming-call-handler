package merail.calls.handler

import android.app.PendingIntent.getActivity
import android.content.Context
import android.os.Looper
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import merail.calls.handler.OperationLogger
import merail.calls.handler.PreferenceHelper
import merail.calls.handler.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.Calendar
import javax.net.ssl.HttpsURLConnection

class UpdateDatabase {
    companion object {
        private val logger = OperationLogger();
        private val preferenceHelper = PreferenceHelper();
    }

    fun updateDatabase(context: Context, urlString: String) {
        val url = URL(urlString)
        val uc: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        val br = BufferedReader(InputStreamReader(uc.getInputStream()))
        var line: String?
        val lin2 = StringBuilder()
        while (br.readLine().also { line = it } != null) {
            lin2.append(line)
        }
        logger.saveToLog(
            context,
            "Loaded numbers from URL " + urlString
        );
    }
}

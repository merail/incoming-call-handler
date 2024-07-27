package merail.calls.handler.workers

import android.app.PendingIntent.getActivity
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import merail.calls.handler.OperationLogger
import merail.calls.handler.PreferenceHelper
import merail.calls.handler.R
import merail.calls.handler.UpdateDatabase
import java.util.Calendar

class UpdateDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    companion object {
        private val logger = OperationLogger();
        private val preferenceHelper = PreferenceHelper();
        private val databaseUpdater = UpdateDatabase();
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//            Get file's URL from shared preferences

        val fileUrl = preferenceHelper.getPreference(
            applicationContext,
            applicationContext.getString(R.string.shared_preference_file_url)
        );

        logger.saveToLog(applicationContext, "Running automatic db update from " + fileUrl)

        preferenceHelper.setPreference(applicationContext, "saved_db_timestamp_pref", System.currentTimeMillis().toString())

        databaseUpdater.updateDatabase(applicationContext, fileUrl!!);

        Result.success(); // todo - handle failure
    }
}

//package merail.calls.handler
//
//import android.content.Context
//import androidx.work.PeriodicWorkRequestBuilder
//import androidx.work.WorkManager
//import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
//import merail.calls.handler.workers.UpdateDatabaseWorker
//import java.util.Calendar
//import java.util.concurrent.TimeUnit
//
//class Scheduler {
//    companion object {
//        private const val jobTag = "blocked_numbers_list_update"
//    }
//
//    fun scheduleAutomaticUpdate(url: String, frequencyInDays: Int) {
//        val workManager = WorkManager.getInstance();
//        //        Remove previous jobs first
//        workManager.cancelAllWorkByTag(jobTag)
//
////        Schedule new jobs
//
//        val saveRequest =
//            PeriodicWorkRequestBuilder<UpdateDatabaseWorker>(15, TimeUnit.MINUTES)
//                // Additional configuration
//                .build()
//
////        val saveRequest =
////            PeriodicWorkRequestBuilder<UpdateDatabaseWorker>(frequencyInDays, TimeUnit.HOURS)
////                // Additional configuration
////                .build()
//
////)
//
//    }
//}

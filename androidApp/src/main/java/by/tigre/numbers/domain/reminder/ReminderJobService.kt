package by.tigre.numbers.domain.reminder

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import by.tigre.logger.Log

@SuppressLint("SpecifyJobSchedulerIdRange")
class ReminderJobService : JobService() {
    init {
        Log.d("ReminderJobService") { "init" }
    }

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d("ReminderJobService", "on start reminder")
        NotificationHelper(this).showNotification()
        return false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }
}
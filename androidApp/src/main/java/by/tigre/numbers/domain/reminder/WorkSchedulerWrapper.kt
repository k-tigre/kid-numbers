package by.tigre.numbers.domain.reminder

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import by.tigre.tools.logger.Log
import kotlin.time.ExperimentalTime

interface WorkSchedulerWrapper {
    fun requestReminderNotification(period: Long)

    fun cancelReminder()

    class Impl(private val context: Context) : WorkSchedulerWrapper {
        private val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as? JobScheduler

        @OptIn(ExperimentalTime::class)
        override fun requestReminderNotification(period: Long) {
            Log.d(TAG) { "requestReminderNotification: $period" }
            val componentName = ComponentName(context, ReminderJobService::class.java)
            val builder = JobInfo.Builder(JOB_ID, componentName)
            builder.setPersisted(true)
            builder.setPeriodic(period)

            jobScheduler?.schedule(builder.build()) ?: Log.e(TAG) { "JobScheduler is NULL" }
        }

        override fun cancelReminder() {
            jobScheduler?.cancel(JOB_ID)
        }

        private companion object {
            const val JOB_ID = 10
            const val TAG = "WorkSchedulerWrapper"
            const val START_TIME = "reminder_start_time"
        }
    }
}


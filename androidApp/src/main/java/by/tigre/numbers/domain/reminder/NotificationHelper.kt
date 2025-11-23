package by.tigre.numbers.domain.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import by.tigre.numbers.App
import by.tigre.numbers.MainActivity
import by.tigre.numbers.R
import by.tigre.numbers.analytics.Event
import by.tigre.tools.logger.Log

class NotificationHelper(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val name = context.getString(R.string.notification_general_channel_title)
        val descriptionText = context.getString(R.string.notification_general_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        notificationManager.createNotificationChannel(mChannel)
    }

    fun showNotification() {
        val graph = (context.applicationContext as? App)?.graph
        if (graph?.reminderController?.isNeedShowReminder() == true) {
            Log.d(TAG) { "NeedShowReminder" }
            graph.eventAnalytics.trackEvent(Event.Action.Logic.ShowReminder)

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(KEY_FROM_REMINDER, true)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_reminder_title))
                .setContentText(context.getString(R.string.notification_reminder_description))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(1, builder.build())
        } else {
            Log.d(TAG) { "No Need Reminder" }
        }
    }

    companion object {
        private const val CHANNEL_ID = "GeneralChannel"
        private const val TAG = "NotificationHelper"

        const val KEY_FROM_REMINDER = "from_reminder"
    }
}
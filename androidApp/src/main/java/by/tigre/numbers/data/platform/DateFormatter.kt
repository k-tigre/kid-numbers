package by.tigre.numbers.data.platform

import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import by.tigre.numbers.R
import java.util.Locale

interface DateFormatter {
    fun formatTime(timestamp: Long): String
    fun formatHoursMinutes(timestamp: Long): String
    fun formatDate(timestamp: Long): String
    fun formatDateTime(timestamp: Long): String
    fun formatDays(timestamp: Long): String

    class Impl(private val resources: Resources) : DateFormatter {
        private val formatterDate = SimpleDateFormat.getDateInstance()
        private val formatterDateTime = SimpleDateFormat.getDateTimeInstance()
        private val formatterTime = SimpleDateFormat.getTimeInstance()
        private val formatterTimeWithoutZone = SimpleDateFormat("H:mm:ss", Locale.US)

        override fun formatTime(timestamp: Long): String = formatterTime.format(timestamp)

        override fun formatDate(timestamp: Long): String = formatterDate.format(timestamp)
        override fun formatDateTime(timestamp: Long): String = formatterDateTime.format(timestamp)

        override fun formatHoursMinutes(timestamp: Long): String = formatterTimeWithoutZone.format(timestamp)

        override fun formatDays(timestamp: Long): String {
            val days = (timestamp / DAY_MILLIS).toInt()
            return resources.getQuantityString(R.plurals.day_count, days, days)
        }
    }

    companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
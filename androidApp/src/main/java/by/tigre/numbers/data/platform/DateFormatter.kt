package by.tigre.numbers.data.platform

import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import by.tigre.numbers.R

interface DateFormatter {
    fun formatTime(timestamp: Long): String
    fun formatTimeWithoutZone(timestamp: Long): String
    fun formatDate(timestamp: Long): String
    fun formatDays(timestamp: Long): String

    class Impl(private val resources: Resources) : DateFormatter {
        private val formatterDate = SimpleDateFormat.getDateInstance()
        private val formatterTime = SimpleDateFormat.getTimeInstance()
        private val formatterTimeWithoutZone = SimpleDateFormat.getTimeInstance().apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        override fun formatTime(timestamp: Long): String = formatterTime.format(timestamp)

        override fun formatDate(timestamp: Long): String = formatterDate.format(timestamp)

        override fun formatTimeWithoutZone(timestamp: Long): String = formatterTimeWithoutZone.format(timestamp)

        override fun formatDays(timestamp: Long): String {
            val days = (timestamp / DAY_MILLIS).toInt()
            return resources.getQuantityString(R.plurals.day_count, days, days)
        }
    }

    companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
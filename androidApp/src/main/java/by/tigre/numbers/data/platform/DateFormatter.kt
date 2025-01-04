package by.tigre.numbers.data.platform

import android.icu.text.SimpleDateFormat

interface DateFormatter {
    fun formatTime(timestamp: Long): String
    fun formatDate(timestamp: Long): String

    class Impl : DateFormatter {
        private val formatterDate = SimpleDateFormat.getDateInstance()
        private val formatterTime = SimpleDateFormat.getTimeInstance()

        override fun formatTime(timestamp: Long): String = formatterTime.format(timestamp)

        override fun formatDate(timestamp: Long): String = formatterDate.format(timestamp)
    }
}
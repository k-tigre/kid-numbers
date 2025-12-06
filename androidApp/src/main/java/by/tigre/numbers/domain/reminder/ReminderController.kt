package by.tigre.numbers.domain.reminder

import by.tigre.numbers.data.storage.Preferences
import by.tigre.tools.logger.Log
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


interface ReminderController {

    fun handleFinishTask()
    fun handleReminderClicked()
    fun handleAppShown()
    fun isNeedShowReminder(): Boolean

    @OptIn(ExperimentalTime::class)
    class Impl(
        private val preferences: Preferences,
        private val workSchedulerWrapper: WorkSchedulerWrapper
    ) : ReminderController {

        override fun handleFinishTask() {
            preferences.saveLong(LAST_FINISH_TIME, Clock.System.now().epochSeconds)
            workSchedulerWrapper.requestReminderNotification(DAY_2_IN_SECONDS * 1000)
        }

        override fun handleReminderClicked() {
            preferences.saveLong(LAST_REMINDER_TIME, Clock.System.now().epochSeconds)
            workSchedulerWrapper.requestReminderNotification(DAY_7_IN_SECONDS * 1000)
        }

        override fun handleAppShown() {
            preferences.saveLong(LAST_APP_SHOWN_TIME, Clock.System.now().epochSeconds)
            workSchedulerWrapper.requestReminderNotification(DAY_4_IN_SECONDS * 1000)
        }

        override fun isNeedShowReminder(): Boolean {
            val current = Clock.System.now().epochSeconds
            val reminder = preferences.loadLong(LAST_REMINDER_TIME, 0) + DAY_7_IN_SECONDS - HOUR_1_IN_SECONDS
            val finish = preferences.loadLong(LAST_FINISH_TIME, 0) + DAY_2_IN_SECONDS - HOUR_1_IN_SECONDS
            val shown = preferences.loadLong(LAST_APP_SHOWN_TIME, 0) + DAY_4_IN_SECONDS - HOUR_1_IN_SECONDS
            Log.i("ReminderController") { "isNeedShowReminder: reminder=${reminder - current} -- finish=${finish - current} -- shown=${shown - current}" }
            return finish < current && reminder < current && shown < current
        }

        private companion object {
            const val LAST_FINISH_TIME = "last_finish_time"
            const val LAST_REMINDER_TIME = "last_reminder_time"
            const val LAST_APP_SHOWN_TIME = "from_reminder"
            const val HOUR_1_IN_SECONDS = 60 * 60L
            const val DAY_1_IN_SECONDS = 24 * HOUR_1_IN_SECONDS
            const val DAY_2_IN_SECONDS = 2 * DAY_1_IN_SECONDS
            const val DAY_4_IN_SECONDS = 4 * DAY_1_IN_SECONDS
            const val DAY_7_IN_SECONDS = 7 * DAY_1_IN_SECONDS
        }
    }
}
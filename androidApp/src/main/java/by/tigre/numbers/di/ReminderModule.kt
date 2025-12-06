package by.tigre.numbers.di

import android.content.Context
import by.tigre.numbers.domain.reminder.ReminderChallengeGenerator
import by.tigre.numbers.domain.reminder.ReminderController
import by.tigre.numbers.domain.reminder.WorkSchedulerWrapper

interface ReminderModule {
    val reminderController: ReminderController
    val reminderChallengeGenerator: ReminderChallengeGenerator

    class Impl(
        context: Context,
        storeModule: StoreModule
    ) : ReminderModule {
        override val reminderController: ReminderController = ReminderController.Impl(
            preferences = storeModule.preferences,
            workSchedulerWrapper = WorkSchedulerWrapper.Impl(context)
        )

        override val reminderChallengeGenerator: ReminderChallengeGenerator = ReminderChallengeGenerator.Impl(
            challengesStore = storeModule.challengesStore,
            resultStore = storeModule.resultStore
        )
    }
}
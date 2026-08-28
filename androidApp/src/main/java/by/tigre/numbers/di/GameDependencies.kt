package by.tigre.numbers.di

import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.numbers.domain.GameProvider
import by.tigre.numbers.domain.OnboardingRepository
import by.tigre.numbers.domain.reminder.ReminderChallengeGenerator
import by.tigre.numbers.domain.reminder.ReminderController
import by.tigre.tools.tools.coroutines.CoreDispatchers


interface GameDependencies {
    fun getGameProvider(): GameProvider
    val resultStore: ResultStore
    val challengesStore: ChallengesStore
    val dispatchers: CoreDispatchers
    val dateFormatter: DateFormatter
    val screenAnalytics: ScreenAnalytics
    val eventAnalytics: EventAnalytics
    val reminderController: ReminderController
    val reminderChallengeGenerator: ReminderChallengeGenerator
    val leaderboardRepository: by.tigre.numbers.data.leaderboard.LeaderboardRepository
    val featureFlags: by.tigre.numbers.data.remoteconfig.FeatureFlags
    val leaderboardPreferences: by.tigre.numbers.data.storage.LeaderboardPreferences
    val leaderboardHistoryBackfill: by.tigre.numbers.data.leaderboard.LeaderboardHistoryBackfill
    val onboardingRepository: OnboardingRepository
}

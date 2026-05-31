package by.tigre.numbers.di

import by.tigre.numbers.data.leaderboard.FirestoreLeaderboardRepository
import by.tigre.numbers.data.leaderboard.LeaderboardHistoryBackfill
import by.tigre.numbers.data.leaderboard.LeaderboardRepository
import by.tigre.numbers.data.remoteconfig.FeatureFlags
import by.tigre.numbers.data.remoteconfig.FeatureFlagsImpl
import by.tigre.numbers.data.remoteconfig.FirebaseRemoteConfigProvider
import by.tigre.numbers.data.remoteconfig.RemoteConfigProvider
import by.tigre.numbers.data.storage.LeaderboardPreferences
import by.tigre.numbers.data.storage.LeaderboardPreferencesImpl
import by.tigre.numbers.data.storage.Preferences

interface LeaderboardModule {
    val leaderboardRepository: LeaderboardRepository
    val featureFlags: FeatureFlags
    val leaderboardPreferences: LeaderboardPreferences
    val leaderboardHistoryBackfill: LeaderboardHistoryBackfill

    class Impl(
        storeModule: StoreModule,
    ) : LeaderboardModule {
        private val remoteConfigProvider: RemoteConfigProvider = FirebaseRemoteConfigProvider()
        override val featureFlags: FeatureFlags = FeatureFlagsImpl(remoteConfigProvider)
        override val leaderboardRepository: LeaderboardRepository = FirestoreLeaderboardRepository()
        override val leaderboardPreferences: LeaderboardPreferences = LeaderboardPreferencesImpl(storeModule.preferences)
        override val leaderboardHistoryBackfill: LeaderboardHistoryBackfill = LeaderboardHistoryBackfill(
            resultStore = storeModule.resultStore,
            leaderboardRepository = leaderboardRepository,
            featureFlags = featureFlags,
            leaderboardPreferences = leaderboardPreferences,
        )
    }
}

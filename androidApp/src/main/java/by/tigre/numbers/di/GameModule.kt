package by.tigre.numbers.di

import by.tigre.numbers.data.remoteconfig.FirebaseRemoteConfigProvider
import by.tigre.numbers.data.remoteconfig.RemoteConfigProvider
import by.tigre.numbers.domain.GameDurationProvider
import by.tigre.numbers.domain.GameProvider

interface GameModule {
    fun getGameProvider(): GameProvider
    fun getDurationProvider(): GameDurationProvider
    class Impl(
        private val analyticsModule: AnalyticsModule,
        private val remoteConfigProvider: RemoteConfigProvider = FirebaseRemoteConfigProvider(),
    ) : GameModule {

        private val gameDurationProvider: GameDurationProvider by lazy {
            GameDurationProvider.Impl(provider = remoteConfigProvider)
        }

        override fun getDurationProvider(): GameDurationProvider = gameDurationProvider

        override fun getGameProvider(): GameProvider = GameProvider.Impl(
            analytics = analyticsModule.eventAnalytics,
            durationProvider = gameDurationProvider
        )
    }
}

package by.tigre.numbers.di

import by.tigre.numbers.domain.GameDurationProvider
import by.tigre.numbers.domain.GameProvider

interface GameModule {
    fun getGameProvider(): GameProvider

    class Impl(
        private val analyticsModule: AnalyticsModule
    ) : GameModule {

        override fun getGameProvider(): GameProvider = GameProvider.Impl(
            analytics = analyticsModule.eventAnalytics,
            durationProvider = GameDurationProvider.Impl()
        )
    }
}

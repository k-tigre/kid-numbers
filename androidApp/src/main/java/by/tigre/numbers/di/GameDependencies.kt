package by.tigre.numbers.di

import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.numbers.domain.GameProvider
import by.tigre.tools.tools.coroutines.CoreDispatchers


interface GameDependencies {
    fun getGameProvider(): GameProvider
    val resultStore: ResultStore
    val dispatchers: CoreDispatchers
    val dateFormatter: DateFormatter
    val screenAnalytics: ScreenAnalytics
    val eventAnalytics: EventAnalytics
}

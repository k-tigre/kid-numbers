package by.tigre.numbers.di

import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.analytics.Tracker
import by.tigre.tools.tools.coroutines.CoroutineModule

interface AnalyticsModule {
    val eventAnalytics: EventAnalytics
    val screenAnalytics: ScreenAnalytics

    class Impl(
        tracker: Tracker,
        coroutineModule: CoroutineModule
    ) : AnalyticsModule {

        override val eventAnalytics: EventAnalytics by lazy { EventAnalytics.Impl(tracker) }

        override val screenAnalytics: ScreenAnalytics by lazy {
            ScreenAnalytics.Impl(
                tracker,
                coroutineModule.dispatchers,
                coroutineModule.scope
            )
        }
    }
}
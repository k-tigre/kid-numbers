package by.tigre.numbers.di

import android.content.Context
import by.tigre.numbers.analytics.Tracker
import by.tigre.tools.tools.coroutines.CoreScope
import by.tigre.tools.tools.coroutines.CoroutineModule

class ApplicationGraph(
    storeModule: StoreModule,
    analyticsModule: AnalyticsModule
) : GameDependencies,
    StoreModule by storeModule,
    AnalyticsModule by analyticsModule {

    companion object {
        fun create(
            context: Context,
            tracker: (CoreScope) -> Tracker
        ): ApplicationGraph {
            val coroutineModule = CoroutineModule.Impl()
            val analyticsModule = AnalyticsModule.Impl(tracker(coroutineModule.scope), coroutineModule)
            val storeModule = StoreModule.Impl(context, coroutineModule = coroutineModule)
            return ApplicationGraph(storeModule, analyticsModule)
        }
    }
}

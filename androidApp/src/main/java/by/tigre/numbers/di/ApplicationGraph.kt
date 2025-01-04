package by.tigre.numbers.di

import android.content.Context
import by.tigre.numbers.analytics.Tracker
import by.tigre.tools.tools.coroutines.CoreScope
import by.tigre.tools.tools.coroutines.CoroutineModule

class ApplicationGraph(
    storeModule: StoreModule,
    analyticsModule: AnalyticsModule,
    gameModule: GameModule
) : GameDependencies,
    StoreModule by storeModule,
    AnalyticsModule by analyticsModule,
    GameModule by gameModule {

    companion object {
        fun create(
            context: Context,
            tracker: (CoreScope) -> Tracker
        ): ApplicationGraph {
            val coroutineModule = CoroutineModule.Impl()
            val analyticsModule = AnalyticsModule.Impl(tracker(coroutineModule.scope), coroutineModule)
            val storeModule = StoreModule.Impl(context, coroutineModule = coroutineModule)
            val gameModule = GameModule.Impl(analyticsModule)
            return ApplicationGraph(storeModule, analyticsModule, gameModule)
        }
    }
}

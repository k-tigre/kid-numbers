package by.tigre.numbers.di

import android.content.Context
import by.tigre.numbers.analytics.Tracker
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.tools.tools.coroutines.CoreScope
import by.tigre.tools.tools.coroutines.CoroutineModule

class ApplicationGraph(
    storeModule: StoreModule,
    analyticsModule: AnalyticsModule,
    gameModule: GameModule,
    coroutineModule: CoroutineModule
) : GameDependencies,
    StoreModule by storeModule,
    AnalyticsModule by analyticsModule,
    GameModule by gameModule,
    CoroutineModule by coroutineModule {

    override val dateFormatter: DateFormatter by lazy { DateFormatter.Impl() }

    companion object {
        fun create(
            context: Context,
            tracker: (CoreScope) -> Tracker
        ): ApplicationGraph {
            val coroutineModule = CoroutineModule.Impl()
            val analyticsModule = AnalyticsModule.Impl(
                tracker = tracker(coroutineModule.scope),
                coroutineModule = coroutineModule
            )
            val storeModule = StoreModule.Impl(
                context = context,
                coroutineModule = coroutineModule,
                analyticsModule = analyticsModule
            )
            val gameModule = GameModule.Impl(
                analyticsModule = analyticsModule
            )
            return ApplicationGraph(
                storeModule = storeModule,
                analyticsModule = analyticsModule,
                gameModule = gameModule,
                coroutineModule = coroutineModule
            )
        }
    }
}

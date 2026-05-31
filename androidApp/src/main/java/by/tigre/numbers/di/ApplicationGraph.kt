package by.tigre.numbers.di

import android.content.Context
import by.tigre.numbers.analytics.Tracker
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.tools.tools.coroutines.CoreScope
import by.tigre.tools.tools.coroutines.CoroutineModule
import kotlinx.coroutines.launch

class ApplicationGraph(
    storeModule: StoreModule,
    analyticsModule: AnalyticsModule,
    gameModule: GameModule,
    coroutineModule: CoroutineModule,
    reminderModule: ReminderModule,
    leaderboardModule: LeaderboardModule,
    context: Context
) : GameDependencies,
    ChallengesDependencies,
    StoreModule by storeModule,
    AnalyticsModule by analyticsModule,
    GameModule by gameModule,
    CoroutineModule by coroutineModule,
    ReminderModule by reminderModule,
    LeaderboardModule by leaderboardModule {

    override val dateFormatter: DateFormatter by lazy { DateFormatter.Impl(context.resources) }

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
            val leaderboardModule = LeaderboardModule.Impl(storeModule = storeModule)
            val reminderModule = ReminderModule.Impl(
                context = context,
                storeModule = storeModule,
            )
            launchRemoteConfigRefresh(coroutineModule, leaderboardModule)
            return ApplicationGraph(
                storeModule = storeModule,
                analyticsModule = analyticsModule,
                gameModule = gameModule,
                coroutineModule = coroutineModule,
                context = context,
                reminderModule = reminderModule,
                leaderboardModule = leaderboardModule,
            )
        }

        private fun launchRemoteConfigRefresh(
            coroutineModule: CoroutineModule,
            leaderboardModule: LeaderboardModule,
        ) {
            coroutineModule.scope.launch {
                leaderboardModule.featureFlags.refreshRemoteConfig()
            }
        }
    }
}

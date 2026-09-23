package by.tigre.numbers.presentation.smart

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.domain.facts.FactCurriculum
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.SmartPracticeSettings
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.game.GameComponent
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import by.tigre.tools.tools.coroutines.CoreDispatchers
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RootSmartGameComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        data object Loading : PageChild
        class Game(val component: GameComponent) : PageChild
        class Result(val component: ResultComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        settings: SmartPracticeSettings,
        dependencies: GameDependencies,
        screenAnalytics: ScreenAnalytics,
        analytics: EventAnalytics,
        private val onClose: () -> Unit,
    ) : RootSmartGameComponent, BaseComponentContext by context {

        private val dispatchers: CoreDispatchers = dependencies.dispatchers
        private val resultStore = dependencies.resultStore
        private val analytics: EventAnalytics = analytics
        private var gameOptions: GameOptions? = null

        private val pagesNavigation = StackNavigation<SmartGamePagesConfig>()

        init {
            launch(dispatchers.io) {
                val options: GameOptions = dependencies.getSmartGameProvider().provide(settings)
                val stats: List<FactStats> = dependencies.factStore.getByOps(settings.enabledOps)
                gameOptions = options
                withContext(dispatchers.main) {
                    analytics.trackEvent(
                        Event.Action.Logic.SmartPracticeStarted(
                            enabledOps = formatOps(settings.enabledOps),
                            factCount = options.questions.size,
                            openBands = formatOpenBands(settings.enabledOps, stats),
                        )
                    )
                    pagesNavigation.replaceCurrent(SmartGamePagesConfig.Game)
                }
            }
        }

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(SmartGamePagesConfig.Loading) },
                key = "smart_game_pages",
                handleBackButton = true,
                serializer = SmartGamePagesConfig.serializer(),
            ) { config, componentContext ->
                when (config) {
                    SmartGamePagesConfig.Loading -> PageChild.Loading
                    SmartGamePagesConfig.Game -> PageChild.Game(
                        GameComponent.Impl(
                            context = componentContext,
                            gameOptions = gameOptions ?: error("Smart game options not loaded"),
                            isSmartPractice = true,
                            reminderController = dependencies.reminderController,
                            analytics = analytics,
                            onFinish = { result ->
                                analytics.trackEvent(
                                    Event.Action.Logic.SmartPracticeFinished(
                                        correct = result.correctCount,
                                        total = result.scoredCount,
                                        durationMs = result.time,
                                        weakCount = result.inCorrectCount,
                                        opsUsed = formatOpsUsed(result),
                                    )
                                )
                                launch(dispatchers.main) { pagesNavigation.replaceCurrent(SmartGamePagesConfig.Result(result)) }
                                launch(dispatchers.io) { resultStore.save(result) }
                            },
                        )
                    )
                    is SmartGamePagesConfig.Result -> PageChild.Result(
                        ResultComponent.Impl(
                            context = componentContext,
                            result = config.result,
                            featureFlags = dependencies.featureFlags,
                            leaderboardRepository = dependencies.leaderboardRepository,
                            leaderboardPreferences = dependencies.leaderboardPreferences,
                            onFinish = onClose,
                        )
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<SmartGamePagesConfig>(screenAnalytics, "SmartGamePagesConfig") {
                    when (it) {
                        SmartGamePagesConfig.Loading -> Event.Screen.RootGame
                        SmartGamePagesConfig.Game -> Event.Screen.RootGame
                        is SmartGamePagesConfig.Result -> Event.Screen.GameResult(
                            correctCount = it.result.correctCount,
                            incorrectCount = it.result.inCorrectCount,
                            totalCount = it.result.totalCount,
                            difficult = it.result.difficult,
                            type = it.result.type,
                        )
                    }
                }
            }
        }

        private fun formatOps(ops: Set<FactKey.Op>): String {
            return ops.sortedBy { it.name }.joinToString(",") { it.name }
        }

        private fun formatOpenBands(enabledOps: Set<FactKey.Op>, stats: List<FactStats>): String {
            return enabledOps.sortedBy { it.name }.joinToString(";") { op ->
                val bands: String = FactCurriculum.openBands(op, stats)
                    .sortedBy { it.index }
                    .joinToString(",") { it.index.toString() }
                "${op.name}:$bands"
            }
        }

        private fun formatOpsUsed(result: GameResult): String {
            return result.results
                .mapNotNull { item ->
                    val operation: Operation = item.question as? Operation ?: return@mapNotNull null
                    FactKey.fromQuestion(operation)?.op
                }
                .distinct()
                .sortedBy { it.name }
                .joinToString(",") { it.name }
        }

        @Serializable
        private sealed interface SmartGamePagesConfig {
            @Serializable
            @SerialName("Loading")
            data object Loading : SmartGamePagesConfig

            @Serializable
            @SerialName("Game")
            data object Game : SmartGamePagesConfig

            @Serializable
            @SerialName("Result")
            data class Result(val result: GameResult) : SmartGamePagesConfig
        }
    }
}

package by.tigre.numbers.presentation.game

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.challenge.result.ChallengeResultComponent
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import by.tigre.tools.presentation.base.toFlow
import by.tigre.tools.tools.coroutines.CoreDispatchers
import by.tigre.tools.tools.coroutines.extensions.tickerFlow
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RootChallengeGameComponent {

    val pages: Value<ChildStack<*, PageChild>>
    val state: StateFlow<State>

    sealed interface PageChild {
        class Game(val component: GameComponent) : PageChild
        class GameResult(val component: ResultComponent) : PageChild
        class ChallengeResult(val component: ChallengeResultComponent) : PageChild
    }

    data class State(
        val taskCount: Int,
        val completedTaskCount: Int,
        val timeLeft: Long,
        val time: String,
        val isCompleted: Boolean
    )

    class Impl(
        context: BaseComponentContext,
        dependencies: ChallengesDependencies,
        private val onClose: () -> Unit,
        challenge: Challenge
    ) : RootChallengeGameComponent, BaseComponentContext by context {

        private val dispatchers: CoreDispatchers = dependencies.dispatchers
        private val formatter: DateFormatter = dependencies.dateFormatter
        private val resultStore = dependencies.resultStore
        private val challengesStore = dependencies.challengesStore
        private val screenAnalytics: ScreenAnalytics = dependencies.screenAnalytics
        private val analytics: EventAnalytics = dependencies.eventAnalytics

        private val pagesNavigation = StackNavigation<GamePagesConfig>()
        private val challengeState = MutableStateFlow(challenge)

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(getNextConfig()) },
                key = "challenge_game_pages",
                handleBackButton = true,
                serializer = GamePagesConfig.serializer()
            ) { config, componentContext ->
                when (config) {
                    is GamePagesConfig.Game -> PageChild.Game(
                        GameComponent.Impl(
                            context = componentContext,
                            settings = config.task.gameSettings,
                            provider = dependencies.getGameProvider(),
                            onFinish = { result ->
                                launch(dispatchers.main) {
                                    pagesNavigation.replaceCurrent(GamePagesConfig.Result(result, config.challengeId))
                                    challengeState.emit(
                                        challengeState.value.let {
                                            it.copy(
                                                tasks = it.tasks.map { task ->
                                                    if (task.id == config.task.id) task.copy(isCompleted = true) else task
                                                }
                                            )
                                        }
                                    )
                                }
                                launch(dispatchers.io) {
                                    resultStore.save(result, challengeId = config.challengeId)
                                    challengesStore.setTaskCompleted(config.task.id)
                                }
                            }
                        )
                    )

                    is GamePagesConfig.Result -> PageChild.GameResult(
                        ResultComponent.Impl(
                            context = componentContext,
                            result = config.result,
                            onFinish = {
                                pagesNavigation.replaceCurrent(getNextConfig())
                            }
                        )
                    )

                    is GamePagesConfig.ChallengeResult -> PageChild.ChallengeResult(
                        ChallengeResultComponent.Impl(
                            context = componentContext,
                            resultStore = resultStore,
                            challengesStore = challengesStore,
                            dateFormatter = dependencies.dateFormatter,
                            challengeId = config.challengeId,
                            onClose = onClose
                        )
                    )

                }
            }

        private var timeTicker = true

        override val state: StateFlow<State> = challengeState
            .combine(tickerFlow(1000, 0).takeWhile { timeTicker }) { state, _ ->
                val completedTaskCount = state.tasks.count { task -> task.isCompleted }
                val taskCount = state.tasks.size
                val timeLeft = (challenge.startDate + challenge.duration.milliseconds) - System.currentTimeMillis()
                val isCompleted = completedTaskCount == taskCount || timeLeft < 0
                if (isCompleted) {
                    challengesStore.setChallengeCompleted(state.id)
                    challengeState.value.tasks.forEach { task ->
                        if (task.isCompleted.not()) {
                            challengesStore.setTaskCompleted(task.id)
                        }
                    }
                    timeTicker = false
                    if (completedTaskCount != taskCount) {
                        launch(dispatchers.main) { pagesNavigation.replaceCurrent(GamePagesConfig.ChallengeResult(challenge.id)) }
                    }
                }
                State(
                    completedTaskCount = completedTaskCount,
                    taskCount = taskCount,
                    timeLeft = timeLeft,
                    isCompleted = isCompleted,
                    time = formatTime(timeLeft),
                )
            }
            .combine(pages.toFlow()) { state, page ->
                when (page.active.instance) {
                    is PageChild.ChallengeResult -> state.copy(isCompleted = true)
                    is PageChild.Game -> state.copy(completedTaskCount = state.completedTaskCount + 1)
                    is PageChild.GameResult -> state.copy(isCompleted = false)
                }
            }
            .stateIn(
                scope = this,
                started = SharingStarted.Eagerly,
                initialValue = State(
                    completedTaskCount = challenge.tasks.count { task -> task.isCompleted },
                    taskCount = challenge.tasks.size,
                    timeLeft = (challenge.startDate + challenge.duration.milliseconds) - System.currentTimeMillis(),
                    isCompleted = false,
                    time = formatTime((challenge.startDate + challenge.duration.milliseconds) - System.currentTimeMillis()),
                )
            )

        private fun formatTime(time: Long): String = if (DateFormatter.DAY_MILLIS < time) {
            formatter.formatDays(time)
        } else {
            formatter.formatTimeWithoutZone(time.coerceAtLeast(0))
        }

        init {
            launch {
                pages.trackScreens<GamePagesConfig>(screenAnalytics) {
                    when (it) {

                        is GamePagesConfig.Game -> Event.Screen.Game(it.task.gameSettings.difficult)

                        is GamePagesConfig.Result -> Event.Screen.GameResult(
                            correctCount = it.result.correctCount,
                            incorrectCount = it.result.inCorrectCount,
                            totalCount = it.result.totalCount,
                            difficult = it.result.difficult,
                            type = it.result.type
                        )

                        is GamePagesConfig.ChallengeResult -> Event.Screen.ChallengeResult
                    }
                }
            }
        }

        private fun getNextConfig(): GamePagesConfig {
            val challenge = challengeState.value
            val task = challenge.tasks.firstOrNull { it.isCompleted.not() }
            return if (task != null) {
                GamePagesConfig.Game(task, challenge.id)
            } else {
                GamePagesConfig.ChallengeResult(challenge.id)
            }
        }

        @Serializable
        private sealed interface GamePagesConfig {

            @Serializable
            @SerialName("Game")
            data class Game(val task: Challenge.Task, val challengeId: String) : GamePagesConfig

            @Serializable
            @SerialName("Result")
            data class Result(val result: GameResult, val challengeId: String) : GamePagesConfig

            @Serializable
            @SerialName("ChallengeResult")
            data class ChallengeResult(
                val challengeId: String
            ) : GamePagesConfig
        }
    }
}

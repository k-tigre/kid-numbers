package by.tigre.numbers.presentation.game

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.numbers.presentation.game.settings.AdditionalSettingsComponent
import by.tigre.numbers.presentation.game.settings.EquationsSettingsComponent
import by.tigre.numbers.presentation.game.settings.GameSettingsComponentProvider
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import by.tigre.tools.tools.coroutines.CoreDispatchers
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RootGameComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class SettingsMultiplication(val component: MultiplicationSettingsComponent) : PageChild
        class SettingsAdditional(val component: AdditionalSettingsComponent) : PageChild
        class SettingsEquations(val component: EquationsSettingsComponent) : PageChild
        class Game(val component: GameComponent) : PageChild
        class Result(val component: ResultComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        gameType: GameType,
        dependencies: GameDependencies,
        screenAnalytics: ScreenAnalytics,
        analytics: EventAnalytics,
        private val onClose: () -> Unit
    ) : RootGameComponent, BaseComponentContext by context {

        private val dispatchers: CoreDispatchers = dependencies.dispatchers
        private val resultStore = dependencies.resultStore

        private val settingsComponentProvider = GameSettingsComponentProvider.Impl(
            analytics = analytics,
            onClose = onClose,
            onConfirmSettings = ::startGame,
            dispatchers = dispatchers
        )

        private val initialSettings = when (gameType) {
            GameType.Additional -> GamePagesConfig.SettingsAdditional(isPositive = true)
            GameType.Multiplication -> GamePagesConfig.SettingsMultiplication(isPositive = true)
            GameType.Division -> GamePagesConfig.SettingsMultiplication(isPositive = false)
            GameType.Subtraction -> GamePagesConfig.SettingsAdditional(isPositive = false)
            GameType.Equations -> GamePagesConfig.SettingsEquations
        }

        private val pagesNavigation = StackNavigation<GamePagesConfig>()

        private fun startGame(settings: GameSettings) {
            launch(dispatchers.main) { pagesNavigation.replaceCurrent(GamePagesConfig.Game(settings)) }
        }

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(initialSettings) },
                key = "game_pages",
                handleBackButton = true,
                serializer = GamePagesConfig.serializer()
            ) { config, componentContext ->
                when (config) {
                    is GamePagesConfig.SettingsMultiplication -> PageChild.SettingsMultiplication(
                        settingsComponentProvider.createMultiplicationSettingsComponent(
                            context = componentContext,
                            isPositive = config.isPositive,
                        )
                    )

                    is GamePagesConfig.SettingsAdditional -> PageChild.SettingsAdditional(
                        settingsComponentProvider.createAdditionalSettingsComponent(
                            context = componentContext,
                            isPositive = config.isPositive,
                        )
                    )

                    is GamePagesConfig.SettingsEquations -> PageChild.SettingsEquations(
                        settingsComponentProvider.createEquationsSettingsComponent(
                            context = componentContext,
                        )
                    )

                    is GamePagesConfig.Game -> PageChild.Game(
                        GameComponent.Impl(
                            context = componentContext,
                            settings = config.settings,
                            provider = dependencies.getGameProvider(),
                            analytics = analytics,
                            onFinish = { result ->
                                launch(dispatchers.main) { pagesNavigation.replaceCurrent(GamePagesConfig.Result(result)) }
                                launch(dispatchers.io) { resultStore.save(result) }
                            }
                        )
                    )

                    is GamePagesConfig.Result -> PageChild.Result(
                        ResultComponent.Impl(
                            context = componentContext,
                            result = config.result,
                            onFinish = onClose
                        )
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<GamePagesConfig>(screenAnalytics, "GamePagesConfig") {
                    when (it) {
                        is GamePagesConfig.SettingsAdditional -> Event.Screen.GameSettings(
                            if (it.isPositive) GameType.Additional else GameType.Subtraction
                        )

                        is GamePagesConfig.SettingsMultiplication -> Event.Screen.GameSettings(
                            if (it.isPositive) GameType.Multiplication else GameType.Division
                        )

                        is GamePagesConfig.SettingsEquations -> Event.Screen.GameSettings(GameType.Equations)

                        is GamePagesConfig.Game -> Event.Screen.Game(it.settings.difficult)

                        is GamePagesConfig.Result -> Event.Screen.GameResult(
                            correctCount = it.result.correctCount,
                            incorrectCount = it.result.inCorrectCount,
                            totalCount = it.result.totalCount,
                            difficult = it.result.difficult,
                            type = it.result.type
                        )
                    }
                }
            }
        }

        @Serializable
        private sealed interface GamePagesConfig {
            @Serializable
            @SerialName("SettingsAdditional")
            data class SettingsAdditional(val isPositive: Boolean) : GamePagesConfig

            @Serializable
            @SerialName("SettingsEquations")
            data object SettingsEquations : GamePagesConfig

            @Serializable
            @SerialName("SettingsMultiplication")
            data class SettingsMultiplication(val isPositive: Boolean) : GamePagesConfig

            @Serializable
            @SerialName("Game")
            data class Game(val settings: GameSettings) : GamePagesConfig

            @Serializable
            @SerialName("Result")
            data class Result(val result: GameResult) : GamePagesConfig
        }
    }
}

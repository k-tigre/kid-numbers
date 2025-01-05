package by.tigre.numbers.presentation.game

import android.os.Parcelable
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
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import by.tigre.tools.tools.coroutines.CoreDispatchers
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

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

        private val initialSettings = when (gameType) {
            GameType.Additional -> PagesConfig.SettingsAdditional(isPositive = true)
            GameType.Multiplication -> PagesConfig.SettingsMultiplication(isPositive = true)
            GameType.Division -> PagesConfig.SettingsMultiplication(isPositive = false)
            GameType.Subtraction -> PagesConfig.SettingsAdditional(isPositive = false)
            GameType.Equations -> PagesConfig.SettingsEquations
        }

        private val pagesNavigation = StackNavigation<PagesConfig>()

        private fun startGame(settings: GameSettings) {
            launch(dispatchers.main) { pagesNavigation.replaceCurrent(PagesConfig.Game(settings)) }
        }

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(initialSettings) },
                key = "game_pages",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    is PagesConfig.SettingsMultiplication -> PageChild.SettingsMultiplication(
                        MultiplicationSettingsComponent.Impl(
                            context = componentContext,
                            isPositive = config.isPositive,
                            onStartGame = ::startGame,
                            onClose = onClose
                        )
                    )

                    is PagesConfig.SettingsAdditional -> PageChild.SettingsAdditional(
                        AdditionalSettingsComponent.Impl(
                            context = componentContext,
                            isPositive = config.isPositive,
                            onStartGame = ::startGame,
                            onClose = onClose,
                            analytics = analytics
                        )
                    )

                    is PagesConfig.SettingsEquations -> PageChild.SettingsEquations(
                        EquationsSettingsComponent.Impl(
                            context = componentContext,
                            onStartGame = ::startGame,
                            onClose = onClose,
                            analytics = analytics
                        )
                    )

                    is PagesConfig.Game -> PageChild.Game(
                        GameComponent.Impl(
                            context = componentContext,
                            settings = config.settings,
                            provider = dependencies.getGameProvider(),
                            onFinish = { result -> launch(dispatchers.main) { pagesNavigation.replaceCurrent(PagesConfig.Result(result)) } }
                        )
                    )

                    is PagesConfig.Result -> PageChild.Result(
                        ResultComponent.Impl(
                            context = componentContext,
                            result = config.result,
                            resultStore = dependencies.resultStore,
                            onFinish = onClose
                        )
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<PagesConfig>(screenAnalytics) {
                    when (it) {
                        is PagesConfig.SettingsAdditional -> Event.Screen.GameSettings(
                            if (it.isPositive) GameType.Additional else GameType.Subtraction
                        )

                        is PagesConfig.SettingsMultiplication -> Event.Screen.GameSettings(
                            if (it.isPositive) GameType.Multiplication else GameType.Division
                        )

                        is PagesConfig.SettingsEquations -> Event.Screen.GameSettings(GameType.Equations)

                        is PagesConfig.Game -> Event.Screen.Game(it.settings.difficult)

                        is PagesConfig.Result -> Event.Screen.GameResult(
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

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data class SettingsAdditional(val isPositive: Boolean) : PagesConfig

            @Parcelize
            data object SettingsEquations : PagesConfig

            @Parcelize
            data class SettingsMultiplication(val isPositive: Boolean) : PagesConfig

            @Parcelize
            data class Game(val settings: GameSettings) : PagesConfig

            @Parcelize
            data class Result(val result: GameResult) : PagesConfig
        }
    }
}

package by.tigre.numbers.presentation.game

import android.os.Parcelable
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.numbers.presentation.game.settings.AdditionalSettingsComponent
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.parcelize.Parcelize

interface RootGameComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class SettingsMultiplication(val component: MultiplicationSettingsComponent) : PageChild
        class SettingsAdditional(val component: AdditionalSettingsComponent) : PageChild
        class Game(val component: GameComponent) : PageChild
        class Result(val component: ResultComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        gameType: GameType,
        gameDependencies: GameDependencies,
        private val onClose: () -> Unit
    ) : RootGameComponent, BaseComponentContext by context {

        private val initialSettings = when (gameType) {
            GameType.Additional -> PagesConfig.SettingsAdditional(isPositive = true)
            GameType.Multiplication -> PagesConfig.SettingsMultiplication(isPositive = true)
            GameType.Division -> PagesConfig.SettingsMultiplication(isPositive = false)
            GameType.Subtraction -> PagesConfig.SettingsAdditional(isPositive = false)
        }

        private val pagesNavigation = StackNavigation<PagesConfig>()

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(initialSettings) },
                key = "pages",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    is PagesConfig.SettingsMultiplication -> PageChild.SettingsMultiplication(
                        MultiplicationSettingsComponent.Impl(
                            context = componentContext,
                            isPositive = config.isPositive,
                            onStartGame = { settings -> pagesNavigation.replaceCurrent(PagesConfig.Game(settings)) },
                            onClose = onClose
                        )
                    )

                    is PagesConfig.SettingsAdditional -> PageChild.SettingsAdditional(
                        AdditionalSettingsComponent.Impl(
                            context = componentContext,
                            isPositive = config.isPositive,
                            onStartGame = { settings -> pagesNavigation.replaceCurrent(PagesConfig.Game(settings)) },
                            onClose = onClose
                        )
                    )


                    is PagesConfig.Game -> PageChild.Game(
                        GameComponent.Impl(
                            context = componentContext,
                            settings = config.settings,
                            provider = gameDependencies.getGameProvider(),
                            onFinish = { result -> pagesNavigation.replaceCurrent(PagesConfig.Result(result)) }
                        )
                    )

                    is PagesConfig.Result -> PageChild.Result(
                        ResultComponent.Impl(
                            context = componentContext,
                            result = config.result,
                            resultStore = gameDependencies.resultStore,
                            onFinish = onClose
                        )
                    )
                }
            }

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data class SettingsAdditional(val isPositive: Boolean) : PagesConfig

            @Parcelize
            data class SettingsMultiplication(val isPositive: Boolean) : PagesConfig

            @Parcelize
            data class Game(val settings: GameSettings) : PagesConfig

            @Parcelize
            data class Result(val result: GameResult) : PagesConfig
        }
    }
}

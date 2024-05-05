package by.tigre.numbers.presentation.game

import android.os.Parcelable
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.result.MultiplicationResultComponent
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
        class Result(val component: MultiplicationResultComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        gameType: GameType,
        gameDependencies: GameDependencies
    ) : RootGameComponent, BaseComponentContext by context {

        private val initialSettings = when (gameType) {
            GameType.Additional -> PagesConfig.SettingsAdditional
            GameType.Multiplication -> PagesConfig.SettingsMultiplication
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
                    PagesConfig.SettingsMultiplication -> PageChild.SettingsMultiplication(
                        MultiplicationSettingsComponent.Impl(componentContext) { settings ->
                            pagesNavigation.replaceCurrent(PagesConfig.Game(settings))
                        }
                    )

                    PagesConfig.SettingsAdditional -> PageChild.SettingsAdditional(
                        AdditionalSettingsComponent.Impl(componentContext) { settings ->
                            pagesNavigation.replaceCurrent(PagesConfig.Game(settings))
                        }
                    )


                    is PagesConfig.Game -> PageChild.Game(
                        GameComponent.Impl(componentContext, config.settings, gameDependencies.getGameProvider()) { result ->
                            pagesNavigation.replaceCurrent(PagesConfig.Result(result))
                        }
                    )

                    is PagesConfig.Result -> PageChild.Result(
                        MultiplicationResultComponent.Impl(componentContext, config.result) {
                            pagesNavigation.replaceCurrent(initialSettings)
                        }
                    )
                }
            }

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data object SettingsAdditional : PagesConfig

            @Parcelize
            data object SettingsMultiplication : PagesConfig

            @Parcelize
            data class Game(val settings: GameSettings) : PagesConfig

            @Parcelize
            data class Result(val result: GameResult) : PagesConfig
        }
    }
}

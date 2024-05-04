package by.tigre.numbers.presentation.multiplication.component

import android.os.Parcelable
import by.tigre.numbers.presentation.multiplication.GameResult
import by.tigre.numbers.presentation.multiplication.GameSettings
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.parcelize.Parcelize

interface RootMultiplicationComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class Settings(val component: MultiplicationSettingsComponent) : PageChild
        class Game(val component: MultiplicationGameComponent) : PageChild
        class Result(val component: MultiplicationResultComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
    ) : RootMultiplicationComponent, BaseComponentContext by context {

        private val pagesNavigation = StackNavigation<PagesConfig>()

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(PagesConfig.Settings) },
                key = "pages",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    PagesConfig.Settings -> PageChild.Settings(
                        MultiplicationSettingsComponent.Impl(componentContext) { settings ->
                            pagesNavigation.replaceCurrent(PagesConfig.Game(settings))
                        }
                    )

                    is PagesConfig.Game -> PageChild.Game(
                        MultiplicationGameComponent.Impl(componentContext, config.settings) { result ->
                            pagesNavigation.replaceCurrent(PagesConfig.Result(result))
                        }
                    )

                    is PagesConfig.Result -> PageChild.Result(
                        MultiplicationResultComponent.Impl(componentContext, config.result) {
                            pagesNavigation.replaceCurrent(PagesConfig.Settings)
                        }
                    )
                }
            }

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data object Settings : PagesConfig

            @Parcelize
            data class Game(val settings: GameSettings) : PagesConfig

            @Parcelize
            data class Result(val result: GameResult) : PagesConfig
        }
    }
}

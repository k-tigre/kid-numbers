package by.tigre.numbers.presentation.root

import android.os.Parcelable
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.RootGameComponent
import by.tigre.numbers.presentation.menu.MenuComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.parcelize.Parcelize

interface RootComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class Menu(val component: MenuComponent) : PageChild
        class Game(val component: RootGameComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        gameDependencies: GameDependencies
    ) : RootComponent, BaseComponentContext by context {

        private val pagesNavigation = StackNavigation<PagesConfig>()

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(PagesConfig.Menu) },
                key = "pages",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    PagesConfig.Menu -> PageChild.Menu(
                        MenuComponent.Impl(componentContext) { type ->
                            pagesNavigation.push(PagesConfig.Game(type))
                        }
                    )

                    is PagesConfig.Game -> PageChild.Game(
                        RootGameComponent.Impl(componentContext, config.type, gameDependencies)
                    )
                }
            }

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data object Menu : PagesConfig

            @Parcelize
            data class Game(val type: GameType) : PagesConfig
        }

    }
}

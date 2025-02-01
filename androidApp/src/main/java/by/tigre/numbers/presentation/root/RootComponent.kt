package by.tigre.numbers.presentation.root

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.game.RootGameComponent
import by.tigre.numbers.presentation.history.HistoryComponent
import by.tigre.numbers.presentation.menu.MenuComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RootComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class Menu(val component: MenuComponent) : PageChild
        class History(val component: HistoryComponent) : PageChild
        class Game(val component: RootGameComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        gameDependencies: GameDependencies,
        screenAnalytics: ScreenAnalytics,
        analytics: EventAnalytics
    ) : RootComponent, BaseComponentContext by context {

        private val pagesNavigation = StackNavigation<MenuPagesConfig>()

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(MenuPagesConfig.Menu) },
                key = "pages",
                handleBackButton = true,
                serializer = MenuPagesConfig.serializer()
            ) { config, componentContext ->
                when (config) {
                    MenuPagesConfig.Menu -> PageChild.Menu(
                        MenuComponent.Impl(
                            context = componentContext,
                            onShowHistory = {
                                pagesNavigation.push(MenuPagesConfig.History)
                            },
                            onGameTypeSelected = { type ->
                                pagesNavigation.push(MenuPagesConfig.Game(type))
                            }
                        )
                    )

                    is MenuPagesConfig.Game -> PageChild.Game(
                        RootGameComponent.Impl(
                            context = componentContext,
                            gameType = config.type,
                            dependencies = gameDependencies,
                            analytics = analytics,
                            onClose = { pagesNavigation.pop() },
                            screenAnalytics = screenAnalytics
                        )
                    )

                    MenuPagesConfig.History -> PageChild.History(
                        HistoryComponent.Impl(
                            context = componentContext,
                            resultStore = gameDependencies.resultStore,
                            dateFormatter = gameDependencies.dateFormatter,
                            onClose = { pagesNavigation.pop() })
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<MenuPagesConfig>(screenAnalytics) {
                    when (it) {
                        MenuPagesConfig.Menu -> Event.Screen.MainMenu
                        MenuPagesConfig.History -> Event.Screen.History
                        is MenuPagesConfig.Game -> Event.Screen.RootGame
                    }
                }
            }
        }

        @Serializable
        private sealed interface MenuPagesConfig {
            @Serializable
            @SerialName("Menu")
            data object Menu : MenuPagesConfig

            @Serializable
            @SerialName("History")
            data object History : MenuPagesConfig

            @Serializable
            @SerialName("Game")
            data class Game(
                @SerialName("GameType")
                val type: GameType
            ) : MenuPagesConfig
        }
    }
}

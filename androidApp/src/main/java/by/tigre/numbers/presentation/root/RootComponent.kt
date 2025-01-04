package by.tigre.numbers.presentation.root

import android.os.Parcelable
import by.tigre.numbers.analytics.Event
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
import kotlinx.parcelize.Parcelize

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
        analytics: ScreenAnalytics,
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
                        MenuComponent.Impl(
                            context = componentContext,
                            onShowHistory = {
                                pagesNavigation.push(PagesConfig.History)
                            },
                            onGameTypeSelected = { type ->
                                pagesNavigation.push(PagesConfig.Game(type))
                            }
                        )
                    )

                    is PagesConfig.Game -> PageChild.Game(
                        RootGameComponent.Impl(
                            context = componentContext,
                            gameType = config.type,
                            dependencies = gameDependencies,
                            analytics = analytics,
                            onClose = { pagesNavigation.pop() })
                    )

                    PagesConfig.History -> PageChild.History(
                        HistoryComponent.Impl(
                            context = componentContext,
                            resultStore = gameDependencies.resultStore,
                            onClose = { pagesNavigation.pop() })
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<PagesConfig>(analytics) {
                    when (it) {
                        PagesConfig.Menu -> Event.Screen.MainMenu
                        PagesConfig.History -> Event.Screen.History
                        is PagesConfig.Game -> Event.Screen.RootGame(it.type)
                    }
                }
            }
        }

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data object Menu : PagesConfig

            @Parcelize
            data object History : PagesConfig

            @Parcelize
            data class Game(val type: GameType) : PagesConfig
        }

    }
}

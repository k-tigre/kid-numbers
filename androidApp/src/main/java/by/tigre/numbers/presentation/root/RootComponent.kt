package by.tigre.numbers.presentation.root

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.challenge.RootChallengeComponent
import by.tigre.numbers.presentation.game.RootChallengeGameComponent
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
        class Challenge(val component: RootChallengeComponent) : PageChild
        class GameChallenge(val component: RootChallengeGameComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        gameDependencies: GameDependencies,
        challengesDependencies: ChallengesDependencies,
        screenAnalytics: ScreenAnalytics,
        analytics: EventAnalytics
    ) : RootComponent, BaseComponentContext by context {

        private val pagesNavigation = StackNavigation<MenuPagesConfig>()

        private val mainMenuRouter = object : MenuComponent.Router {
            override fun showGameSettings(type: GameType) {
                pagesNavigation.push(MenuPagesConfig.Game(type))
            }

            override fun showHistory() {
                pagesNavigation.push(MenuPagesConfig.History)
            }

            override fun showChallenge() {
                pagesNavigation.push(MenuPagesConfig.Challenge)
            }
        }

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
                            router = mainMenuRouter,
                            challengesDependencies = challengesDependencies
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

                    MenuPagesConfig.Challenge -> PageChild.Challenge(
                        RootChallengeComponent.Impl(
                            context = componentContext,
                            onClose = { pagesNavigation.pop() },
                            analytics = analytics,
                            screenAnalytics = screenAnalytics,
                            dependencies = challengesDependencies,
                            onStartChallenge = { challenge -> pagesNavigation.push(MenuPagesConfig.ChallengeGame(challenge)) }
                        )
                    )

                    is MenuPagesConfig.ChallengeGame -> PageChild.GameChallenge(
                        RootChallengeGameComponent.Impl(
                            context = componentContext,
                            dependencies = challengesDependencies,
                            onClose = { pagesNavigation.pop() },
                            challenge = config.challenge
                        )
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<MenuPagesConfig>(screenAnalytics) {
                    when (it) {
                        MenuPagesConfig.Menu -> Event.Screen.MainMenu
                        MenuPagesConfig.History -> Event.Screen.History
                        MenuPagesConfig.Challenge -> Event.Screen.RootChallenge
                        is MenuPagesConfig.ChallengeGame -> Event.Screen.RootGameChallenge
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
            @SerialName("Challenge")
            data object Challenge : MenuPagesConfig

            @Serializable
            @SerialName("Game")
            data class Game(
                @SerialName("GameType")
                val type: GameType
            ) : MenuPagesConfig

            @Serializable
            @SerialName("ChallengeGame")
            data class ChallengeGame(val challenge: by.tigre.numbers.entity.Challenge) : MenuPagesConfig
        }
    }
}

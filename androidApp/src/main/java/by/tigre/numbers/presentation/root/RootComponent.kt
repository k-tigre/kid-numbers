package by.tigre.numbers.presentation.root

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.di.GameDependencies
import by.tigre.numbers.domain.reminder.ReminderChallengeGenerator
import by.tigre.numbers.domain.reminder.ReminderController
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.challenge.RootChallengeComponent
import by.tigre.numbers.presentation.game.RootChallengeGameComponent
import by.tigre.numbers.presentation.game.RootGameComponent
import by.tigre.numbers.presentation.history.HistoryComponent
import by.tigre.numbers.presentation.leaderboard.LeaderboardComponentImpl
import by.tigre.numbers.presentation.menu.MenuComponent
import by.tigre.numbers.presentation.statistic.StatisticComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildSlot
import by.tigre.tools.presentation.base.appChildStack
import by.tigre.tools.tools.coroutines.CoreDispatchers
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RootComponent {

    val pages: Value<ChildStack<*, PageChild>>
    val dialogs: Value<ChildSlot<*, DialogChild>>

    fun onDismissDialog()
    fun onStartChallengeFromReminder(challengeId: String)
    fun onShowChallenges()
    fun onReminderLaterClicked(withChallenge: Boolean)

    sealed interface PageChild {
        class Menu(val component: MenuComponent) : PageChild
        class History(val component: HistoryComponent) : PageChild
        class Statistic(val component: StatisticComponent) : PageChild
        class Leaderboard(val component: by.tigre.numbers.presentation.leaderboard.LeaderboardComponent) : PageChild
        class Game(val component: RootGameComponent) : PageChild
        class Challenge(val component: RootChallengeComponent) : PageChild
        class GameChallenge(val component: RootChallengeGameComponent) : PageChild
    }

    sealed interface DialogChild {
        data class Reminder(val challengeId: String) : DialogChild
        data object ReminderHasChallenge : DialogChild
    }

    class Impl(
        context: BaseComponentContext,
        gameDependencies: GameDependencies,
        challengesDependencies: ChallengesDependencies,
        screenAnalytics: ScreenAnalytics,
        private val analytics: EventAnalytics,
        fromReminder: Boolean
    ) : RootComponent, BaseComponentContext by context {

        private val challengeStore: ChallengesStore = challengesDependencies.challengesStore
        private val reminderController: ReminderController = challengesDependencies.reminderController
        private val reminderChallengeGenerator: ReminderChallengeGenerator = challengesDependencies.reminderChallengeGenerator
        private val dispatchers: CoreDispatchers = challengesDependencies.dispatchers

        private val pagesNavigation = StackNavigation<MenuPagesConfig>()
        private val dialogsNavigation = SlotNavigation<DialogsConfig>()
        private val mainMenuRouter = object : MenuComponent.Router {
            override fun showGameSettings(type: GameType) {
                pagesNavigation.pushNew(MenuPagesConfig.Game(type))
            }

            override fun showHistory() {
                pagesNavigation.pushNew(MenuPagesConfig.History)
            }

            override fun showChallenge() {
                pagesNavigation.pushNew(MenuPagesConfig.Challenge)
            }

            override fun showStatistic() {
                pagesNavigation.pushNew(MenuPagesConfig.Statistic)
            }

            override fun showLeaderboard() {
                pagesNavigation.pushNew(MenuPagesConfig.Leaderboard)
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
                            challengesStore = gameDependencies.challengesStore,
                            dateFormatter = gameDependencies.dateFormatter,
                            featureFlags = gameDependencies.featureFlags,
                            leaderboardRepository = gameDependencies.leaderboardRepository,
                            leaderboardPreferences = gameDependencies.leaderboardPreferences,
                            onClose = { pagesNavigation.pop() })
                    )

                    MenuPagesConfig.Statistic -> PageChild.Statistic(
                        StatisticComponent.Impl(
                            context = componentContext,
                            resultStore = gameDependencies.resultStore,
                            onClose = { pagesNavigation.pop() }
                        )
                    )

                    MenuPagesConfig.Leaderboard -> PageChild.Leaderboard(
                        LeaderboardComponentImpl(
                            context = componentContext,
                            leaderboardRepository = gameDependencies.leaderboardRepository,
                            leaderboardHistoryBackfill = gameDependencies.leaderboardHistoryBackfill,
                            navigateBack = { pagesNavigation.pop() },
                        )
                    )

                    MenuPagesConfig.Challenge -> PageChild.Challenge(
                        RootChallengeComponent.Impl(
                            context = componentContext,
                            onClose = { pagesNavigation.pop() },
                            analytics = analytics,
                            screenAnalytics = screenAnalytics,
                            dependencies = challengesDependencies,
                            onStartChallenge = { challenge -> pagesNavigation.pushNew(MenuPagesConfig.ChallengeGame(challenge)) }
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

        override val dialogs: Value<ChildSlot<*, DialogChild>> =
            appChildSlot(
                source = dialogsNavigation,
                serializer = DialogsConfig.serializer(),
                key = "root_dialogs",
                handleBackButton = true,
            ) { config, _ ->
                when (config) {
                    is DialogsConfig.Reminder -> DialogChild.Reminder(config.challengeId)
                    is DialogsConfig.ReminderHasChallenge -> DialogChild.ReminderHasChallenge
                }
            }

        init {
            launch {
                pages.trackScreens<MenuPagesConfig>(screenAnalytics, "MenuPagesConfig") {
                    when (it) {
                        MenuPagesConfig.Menu -> Event.Screen.MainMenu
                        MenuPagesConfig.History -> Event.Screen.History
                        MenuPagesConfig.Statistic -> Event.Screen.Statistic
                        MenuPagesConfig.Leaderboard -> Event.Screen.Leaderboard
                        MenuPagesConfig.Challenge -> Event.Screen.RootChallenge
                        is MenuPagesConfig.ChallengeGame -> Event.Screen.RootGameChallenge
                        is MenuPagesConfig.Game -> Event.Screen.RootGame
                    }
                }
            }

            if (fromReminder) {
                reminderController.handleReminderClicked()
                launch {
                    val challengeId = reminderChallengeGenerator.generateIfNeeded()
                    withContext(dispatchers.main) {
                        if (challengeId != null) {
                            dialogsNavigation.activate(DialogsConfig.Reminder(challengeId))
                        } else {
                            dialogsNavigation.activate(DialogsConfig.ReminderHasChallenge)
                        }
                    }
                }
            } else {
                reminderController.handleAppShown()
            }
        }

        override fun onDismissDialog() {
            dialogsNavigation.dismiss()
        }

        override fun onStartChallengeFromReminder(challengeId: String) {
            dialogsNavigation.dismiss()
            analytics.trackEvent(Event.Action.UI.Button.ReminderStartClicked)
            launch {
                challengeStore.start(challengeId)
                val challenge = challengeStore.getChallenge(challengeId)
                if (challenge != null) {
                    withContext(dispatchers.main) {
                        pagesNavigation.pushNew(MenuPagesConfig.ChallengeGame(challenge))
                    }
                } else {
                    analytics.trackEvent(Event.Action.Logic.FailedToFindReminderChallenge)
                }
            }
        }

        override fun onShowChallenges() {
            dialogsNavigation.dismiss()
            analytics.trackEvent(Event.Action.UI.Button.ReminderShowChallengesClicked)
            mainMenuRouter.showChallenge()
        }

        override fun onReminderLaterClicked(withChallenge: Boolean) {
            dialogsNavigation.dismiss()
            analytics.trackEvent(Event.Action.UI.Button.ReminderLaterClicked(withChallenge))
        }

        @Serializable
        private sealed interface MenuPagesConfig {
            @Serializable
            @SerialName("MenuPagesConfig_Menu")
            data object Menu : MenuPagesConfig

            @Serializable
            @SerialName("MenuPagesConfig_History")
            data object History : MenuPagesConfig

            @Serializable
            @SerialName("MenuPagesConfig_Statistic")
            data object Statistic : MenuPagesConfig

            @Serializable
            @SerialName("MenuPagesConfig_Leaderboard")
            data object Leaderboard : MenuPagesConfig

            @Serializable
            @SerialName("MenuPagesConfig_Challenge")
            data object Challenge : MenuPagesConfig

            @Serializable
            @SerialName("MenuPagesConfig_Game")
            data class Game(
                @SerialName("GameType")
                val type: GameType
            ) : MenuPagesConfig

            @Serializable
            @SerialName("MenuPagesConfig_ChallengeGame")
            data class ChallengeGame(val challenge: by.tigre.numbers.entity.Challenge) : MenuPagesConfig
        }

        @Serializable
        private sealed interface DialogsConfig {
            @Serializable
            @SerialName("Reminder")
            data class Reminder(
                @SerialName("challengeId")
                val challengeId: String
            ) : DialogsConfig

            @Serializable
            @SerialName("ReminderHasChallenge")
            data object ReminderHasChallenge : DialogsConfig
        }
    }
}

package by.tigre.numbers.presentation.challenge

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.extension.trackScreens
import by.tigre.numbers.presentation.challenge.creator.DetailsComponent
import by.tigre.numbers.presentation.challenge.list.ListComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import by.tigre.tools.tools.coroutines.CoreDispatchers
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RootChallengeComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class List(val component: ListComponent) : PageChild
        class Details(val component: DetailsComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
        screenAnalytics: ScreenAnalytics,
        analytics: EventAnalytics,
        dependencies: ChallengesDependencies,
        private val onClose: () -> Unit,
        private val onStartChallenge: (Challenge) -> Unit
    ) : RootChallengeComponent, BaseComponentContext by context {

        private val dispatchers: CoreDispatchers = dependencies.dispatchers

        private val pagesNavigation = StackNavigation<ChallengePagesConfig>()

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(ChallengePagesConfig.List) },
                serializer = ChallengePagesConfig.serializer(),
                key = "pages_Challenge",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    is ChallengePagesConfig.List -> PageChild.List(
                        ListComponent.Impl(
                            context = componentContext,
                            onClose = onClose,
                            dependencies = dependencies,
                            onCreate = { pagesNavigation.push(ChallengePagesConfig.Details(null)) },
                            onEdit = { pagesNavigation.push(ChallengePagesConfig.Details(it)) },
                            onStart = onStartChallenge
                        )
                    )

                    is ChallengePagesConfig.Details -> PageChild.Details(
                        DetailsComponent.Impl(
                            context = componentContext,
                            screenAnalytics = screenAnalytics,
                            analytics = analytics,
                            dependencies = dependencies,
                            challengeId = config.challengeId,
                            onClose = {
                                launch { withContext(dispatchers.main) { pagesNavigation.pop() } }
                            },
                        )
                    )
                }
            }

        init {
            launch {
                pages.trackScreens<ChallengePagesConfig>(screenAnalytics, "ChallengePagesConfig") {
                    when (it) {
                        ChallengePagesConfig.List -> Event.Screen.ChallengesList
                        is ChallengePagesConfig.Details -> Event.Screen.ChallengeDetails
                    }
                }
            }
        }

        @Serializable
        private sealed interface ChallengePagesConfig {
            @Serializable
            @SerialName("ChallengePagesConfig_List")
            data object List : ChallengePagesConfig

            @Serializable
            @SerialName("ChallengePagesConfig_Details")
            data class Details(val challengeId: String?) : ChallengePagesConfig
        }
    }
}

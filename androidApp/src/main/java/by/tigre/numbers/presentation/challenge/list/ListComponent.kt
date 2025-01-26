package by.tigre.numbers.presentation.challenge.list

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.ChallengeWithCount
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.tools.coroutines.CoreDispatchers
import by.tigre.tools.tools.coroutines.extensions.tickerFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ListComponent {

    val challenges: StateFlow<List<ChallengeItem>>
    fun onCloseClicked()
    fun onCreateClicked()
    fun onViewClicked(challenge: ChallengeItem)
    fun onStartClicked(challenge: ChallengeItem)

    data class ChallengeItem(
        val challenge: ChallengeWithCount,
        val isDied: Boolean,
    )

    class Impl(
        context: BaseComponentContext,
        dependencies: ChallengesDependencies,
        private val onClose: () -> Unit,
        private val onCreate: () -> Unit,
        private val onEdit: (String) -> Unit,
        private val onStart: (Challenge) -> Unit
    ) : ListComponent, BaseComponentContext by context {
        private val store: ChallengesStore = dependencies.challengesStore
        private val analytics: EventAnalytics = dependencies.eventAnalytics
        private val dispatchers: CoreDispatchers = dependencies.dispatchers

        override val challenges: StateFlow<List<ChallengeItem>> = store.challenges
            .combine(tickerFlow(10_000, 0)) { challenges, _ ->
                challenges.map { item ->
                    ChallengeItem(
                        challenge = item,
                        isDied = item.status == Challenge.Status.Active && item.startDate + item.duration.milliseconds < System.currentTimeMillis()
                    )
                }
            }
            .stateIn(
                scope = this,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

        override fun onCloseClicked() = onClose()
        override fun onCreateClicked() = onCreate()
        override fun onViewClicked(challenge: ChallengeItem) = onEdit(challenge.challenge.id)
        override fun onStartClicked(challenge: ChallengeItem) {
            launch {
                if (challenge.challenge.status != Challenge.Status.Active) {
                    store.start(challenge.challenge.id)
                }
                val fullChallenge = store.getChallenge(challenge.challenge.id)
                if (fullChallenge == null) {
                    analytics.trackEvent(Event.Action.Logic.Error(type = "CanNotFindChallengeInDB"))
                    return@launch
                }

                if (challenge.isDied) {
                    fullChallenge.tasks.forEach { task ->
                        if (task.isCompleted.not()) {
                            store.setTaskCompleted(task.id)
                        }
                    }
                    store.setChallengeCompleted(challenge.challenge.id)
                } else {
                    withContext(dispatchers.main) {
                        onStart(fullChallenge)
                    }
                }
            }
        }
    }
}

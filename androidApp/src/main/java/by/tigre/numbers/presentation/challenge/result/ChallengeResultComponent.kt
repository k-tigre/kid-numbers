package by.tigre.numbers.presentation.challenge.result

import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildSlot
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface ChallengeResultComponent {

    val results: StateFlow<ScreenState>
    val details: Value<ChildSlot<*, ResultComponent>>

    fun onCloseClicked()
    fun onItemClicked(item: HistoryItem)

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class History(
            val items: List<HistoryItem>,
            val startDate: Long,
            val endDate: Long,
            val duration: Challenge.Duration,
            val isSuccess: Boolean
        ) : ScreenState
    }

    data class HistoryItem(
        val id: Long,
        val time: String,
        val duration: Long,
        val difficult: Difficult,
        val gameType: GameType?,
        val correctCount: Int,
        val totalCount: Int
    ) {
        val isCorrect = correctCount == totalCount
    }

    class Impl(
        context: BaseComponentContext,
        private val resultStore: ResultStore,
        private val challengesStore: ChallengesStore,
        private val dateFormatter: DateFormatter,
        private val challengeId: String,
        private val onClose: () -> Unit
    ) : ChallengeResultComponent, BaseComponentContext by context {

        private val detailsNavigation = SlotNavigation<DetailsItemConfig>()

        override val details: Value<ChildSlot<*, ResultComponent>> =
            appChildSlot(
                source = detailsNavigation,
                serializer = DetailsItemConfig.serializer(),
                handleBackButton = true,
            ) { config, childComponentContext ->
                ResultComponent.Impl(
                    context = childComponentContext,
                    result = config.result,
                    onFinish = detailsNavigation::dismiss
                )
            }

        override val results: StateFlow<ScreenState> = flow<ScreenState> {
            val items = resultStore.loadForChallenge(challengeId)
            val challenge = challengesStore.getChallenge(challengeId)
            if (challenge == null) {
                emit(ScreenState.Loading)
                // log
                return@flow
            }

            val state = ScreenState.History(
                items = items.map { item ->
                    HistoryItem(
                        id = item.id,
                        time = dateFormatter.formatTime(item.date),
                        duration = item.duration,
                        difficult = item.difficult,
                        gameType = item.gameType,
                        correctCount = item.correctCount,
                        totalCount = item.totalCount,
                    )
                },
                endDate = challenge.endDate,
                isSuccess = items.all { it.correctCount == it.totalCount },
                startDate = challenge.startDate,
                duration = challenge.duration,
            )
            emit(state)
        }
            .stateIn(this, SharingStarted.WhileSubscribed(), ScreenState.Loading)

        override fun onCloseClicked() {
            onClose()
        }

        override fun onItemClicked(item: HistoryItem) {
            launch {
                resultStore.getDetails(item.id)?.let {
                    detailsNavigation.activate(DetailsItemConfig(it))
                }
            }
        }

        @Serializable
        private data class DetailsItemConfig(
            val result: GameResult
        )
    }
}

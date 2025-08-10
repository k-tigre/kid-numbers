package by.tigre.numbers.presentation.history

import androidx.compose.foundation.pager.PagerState
import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.platform.DateFormatter
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.challenge.result.ChallengeResultComponent
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildSlot
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface HistoryComponent {
    val pagerState: PagerState
    val resultsTasks: StateFlow<ScreenStateTasks>
    val resultsChallenges: StateFlow<ScreenStateChallenges>
    val filter: StateFlow<Filter>
    val filterVisibility: StateFlow<Boolean>
    val details: Value<ChildSlot<*, Any>>

    fun onFilterVisibleChanges(visible: Boolean)
    fun onDifficultFilterChanges(difficult: Difficult, isEnabled: Boolean)
    fun onGameTypeFilterChanges(type: GameType, isEnabled: Boolean)
    fun onOnlySuccessTaskChanges(isEnabled: Boolean)
    fun onOnlySuccessChallengesChanges(isEnabled: Boolean)
    fun onGroupExpandChanges(isExpanded: Boolean, group: HistoryGroup)
    fun onCloseClicked()
    fun onCloseItemClicked()
    fun onItemClicked(item: HistoryItem)
    fun onItemClicked(item: ChallengeItem)

    sealed interface ScreenStateTasks {
        data object Loading : ScreenStateTasks
        data class Empty(val withFilter: Boolean) : ScreenStateTasks
        data class History(val groups: List<HistoryGroup>) : ScreenStateTasks
    }

    sealed interface ScreenStateChallenges {
        data object Loading : ScreenStateChallenges
        data class Empty(val withFilter: Boolean) : ScreenStateChallenges
        data class History(val challenges: List<ChallengeItem>) : ScreenStateChallenges
    }

    data class HistoryGroup(
        val date: String,
        val items: List<HistoryItem>,
        val isExpanded: Boolean
    )

    data class Filter(
        val difficult: Map<Difficult, Boolean>,
        val gameType: Map<GameType, Boolean>,
        val onlySuccessTasks: Boolean,
        val onlySuccessChallenges: Boolean,
    )

    data class HistoryItem(
        val id: Long,
        val time: String,
        val duration: Long,
        val difficult: Difficult,
        val gameType: GameType?,
        val correctCount: Int,
        val totalCount: Int
    )

    data class ChallengeItem(
        val id: String,
        val time: String,
        val duration: Long,
        val totalCount: Int,
        val isSuccess: Boolean
    )

    class Impl(
        context: BaseComponentContext,
        private val resultStore: ResultStore,
        private val challengesStore: ChallengesStore,
        dateFormatter: DateFormatter,
        private val onClose: () -> Unit
    ) : HistoryComponent, BaseComponentContext by context {
        private val expendedGroup = MutableStateFlow(setOf<String>())
        private val detailsNavigation = SlotNavigation<DetailsItemConfig>()

        override val filterVisibility = MutableStateFlow(false)
        override val filter = MutableStateFlow(DEFAULT_FILTER)

        override val pagerState: PagerState = PagerState(0, pageCount = { 2 })

        override val details: Value<ChildSlot<*, Any>> =
            appChildSlot(
                source = detailsNavigation,
                serializer = DetailsItemConfig.serializer(),
                handleBackButton = true,
            ) { config, childComponentContext ->
                when (config) {
                    is DetailsItemConfig.Challenge -> ChallengeResultComponent.Impl(
                        context = childComponentContext,
                        resultStore = resultStore,
                        challengesStore = challengesStore,
                        dateFormatter = dateFormatter,
                        challengeId = config.id,
                        onClose = detailsNavigation::dismiss
                    )

                    is DetailsItemConfig.Task -> ResultComponent.Impl(
                        context = childComponentContext,
                        result = config.result,
                        onFinish = detailsNavigation::dismiss
                    )
                }
            }

        override val resultsChallenges: StateFlow<ScreenStateChallenges> = filter
            .map { filter ->
                val history = resultStore.loadCompletedChallenges(filter.onlySuccessChallenges)
                history.map {
                    ChallengeItem(
                        id = it.id,
                        time = dateFormatter.formatDateTime(it.startTime),
                        duration = it.duration,
                        totalCount = it.taskCount,
                        isSuccess = it.isSuccess
                    )
                } to filter
            }.map { (history, filter) ->

                if (history.isNotEmpty()) {
                    ScreenStateChallenges.History(challenges = history)
                } else {
                    ScreenStateChallenges.Empty(withFilter = filter.onlySuccessChallenges != DEFAULT_FILTER.onlySuccessChallenges)
                }
            }
            .stateIn(this, SharingStarted.WhileSubscribed(), ScreenStateChallenges.Loading)

        override val resultsTasks: StateFlow<ScreenStateTasks> = filter
            .map { filter ->
                val history = resultStore.load(
                    difficult = filter.difficult.mapNotNull { (difficult, isEnabled) -> difficult.takeIf { isEnabled } },
                    types = filter.gameType.mapNotNull { (gameType, isEnabled) -> gameType.takeIf { isEnabled } },
                    onlySuccess = filter.onlySuccessTasks
                )
                history.groupBy { dateFormatter.formatDate(it.date) } to filter
            }
            .combine(expendedGroup) { (history, filter), expended ->
                val groups = history.map { (date, items) ->
                    HistoryGroup(
                        date = date,
                        isExpanded = expended.contains(date),
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
                        }
                    )
                }
                if (groups.isNotEmpty()) {
                    ScreenStateTasks.History(groups = groups)
                } else {
                    ScreenStateTasks.Empty(withFilter = filter != DEFAULT_FILTER)
                }
            }
            .stateIn(this, SharingStarted.WhileSubscribed(), ScreenStateTasks.Loading)

        override fun onCloseClicked() {
            if (filterVisibility.value) {
                filterVisibility.tryEmit(false)
            } else {
                onClose()
            }
        }

        override fun onFilterVisibleChanges(visible: Boolean) {
            filterVisibility.tryEmit(visible)
        }

        override fun onItemClicked(item: HistoryItem) {
            launch {
                resultStore.getDetails(item.id)?.let {
                    detailsNavigation.activate(DetailsItemConfig.Task(it))
                }
            }
        }

        override fun onItemClicked(item: ChallengeItem) {
            detailsNavigation.activate(DetailsItemConfig.Challenge(item.id))
        }

        override fun onCloseItemClicked() {
            detailsNavigation.dismiss()
        }

        override fun onDifficultFilterChanges(difficult: Difficult, isEnabled: Boolean) {
            launch {
                filter.emit(
                    filter.value.let { filter ->
                        filter.copy(difficult = filter.difficult.mapValues { if (it.key == difficult) isEnabled else it.value })
                    }
                )
            }
        }

        override fun onGameTypeFilterChanges(type: GameType, isEnabled: Boolean) {
            launch {
                filter.emit(
                    filter.value.let { filter ->
                        filter.copy(gameType = filter.gameType.mapValues { if (it.key == type) isEnabled else it.value })
                    }
                )
            }
        }

        override fun onOnlySuccessTaskChanges(isEnabled: Boolean) {
            launch {
                filter.emit(
                    filter.value.copy(onlySuccessTasks = isEnabled)
                )
            }
        }

        override fun onOnlySuccessChallengesChanges(isEnabled: Boolean) {
            launch {
                filter.emit(
                    filter.value.copy(onlySuccessChallenges = isEnabled)
                )
            }
        }

        override fun onGroupExpandChanges(isExpanded: Boolean, group: HistoryGroup) {
            launch {
                expendedGroup.emit(
                    if (isExpanded) {
                        expendedGroup.value + group.date
                    } else {
                        expendedGroup.value - group.date
                    }
                )
            }
        }

        @Serializable
        private sealed interface DetailsItemConfig {
            @Serializable
            data class Task(val result: GameResult) : DetailsItemConfig

            @Serializable
            data class Challenge(val id: String) : DetailsItemConfig
        }

        private companion object {
            val DEFAULT_FILTER = Filter(
                difficult = Difficult.entries.associateWith { true },
                gameType = GameType.entries.associateWith { true },
                onlySuccessTasks = false,
                onlySuccessChallenges = false
            )
        }
    }
}

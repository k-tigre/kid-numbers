package by.tigre.numbers.presentation.history

import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.platform.DateFormatter
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface HistoryComponent {

    val results: StateFlow<ScreenState>
    val filter: StateFlow<Filter>
    val filterVisibility: StateFlow<Boolean>
    val details: Value<ChildSlot<*, ResultComponent>>

    fun onFilterVisibleChanges(visible: Boolean)
    fun onDifficultFilterChanges(difficult: Difficult, isEnabled: Boolean)
    fun onGameTypeFilterChanges(type: GameType, isEnabled: Boolean)
    fun onOnlySuccessChanges(isEnabled: Boolean)
    fun onGroupExpandChanges(isExpanded: Boolean, group: HistoryGroup)
    fun onCloseClicked()
    fun onCloseItemClicked()
    fun onItemClicked(item: HistoryItem)

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Empty(val withFilter: Boolean) : ScreenState
        data class History(val groups: List<HistoryGroup>) : ScreenState
    }

    data class HistoryGroup(
        val date: String,
        val items: List<HistoryItem>,
        val isExpanded: Boolean
    )

    data class Filter(val difficult: Map<Difficult, Boolean>, val gameType: Map<GameType, Boolean>, val onlySuccess: Boolean)
    data class HistoryItem(
        val id: Long,
        val time: String,
        val duration: Long,
        val difficult: Difficult,
        val gameType: GameType?,
        val correctCount: Int,
        val totalCount: Int
    )

    class Impl(
        context: BaseComponentContext,
        private val resultStore: ResultStore,
        dateFormatter: DateFormatter,
        private val onClose: () -> Unit
    ) : HistoryComponent, BaseComponentContext by context {
        private val expendedGroup = MutableStateFlow(setOf<String>())
        private val detailsNavigation = SlotNavigation<DetailsItemConfig>()

        override val filterVisibility = MutableStateFlow(false)
        override val filter = MutableStateFlow(DEFAULT_FILTER)

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

        override val results: StateFlow<ScreenState> = filter
            .map { filter ->
                val history = resultStore.load(
                    difficult = filter.difficult.mapNotNull { (difficult, isEnabled) -> difficult.takeIf { isEnabled } },
                    types = filter.gameType.mapNotNull { (gameType, isEnabled) -> gameType.takeIf { isEnabled } },
                    onlySuccess = filter.onlySuccess
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
                    ScreenState.History(groups)
                } else {
                    ScreenState.Empty(withFilter = filter != DEFAULT_FILTER)
                }
            }
            .stateIn(this, SharingStarted.WhileSubscribed(), ScreenState.Loading)

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
                    detailsNavigation.activate(DetailsItemConfig(it))
                }
            }
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

        override fun onOnlySuccessChanges(isEnabled: Boolean) {
            launch {
                filter.emit(
                    filter.value.copy(onlySuccess = isEnabled)
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
        private data class DetailsItemConfig(
            val result: GameResult
        )

        private companion object {
            val DEFAULT_FILTER = Filter(
                difficult = Difficult.entries.associateWith { true },
                gameType = GameType.entries.associateWith { true },
                onlySuccess = false
            )
        }
    }
}

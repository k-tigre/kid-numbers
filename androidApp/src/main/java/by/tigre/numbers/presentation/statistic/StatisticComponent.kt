package by.tigre.numbers.presentation.statistic

import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.domain.facts.FactMasterySnapshot
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.StatisticData
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.tools.coroutines.CoreDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface StatisticComponent {

    val screenState: StateFlow<ScreenState>

    fun onCloseClicked()

    sealed interface ScreenState {
        data object Loading : ScreenState
        data object Empty : ScreenState
        data class Data(
            val statistic: StatisticData,
            val gameTypes: List<GameType>,
            val mastery: FactMasterySnapshot,
        ) : ScreenState
    }

    class Impl(
        context: BaseComponentContext,
        private val resultStore: ResultStore,
        private val factStore: FactStore,
        private val dispatchers: CoreDispatchers,
        private val onClose: () -> Unit,
    ) : StatisticComponent, BaseComponentContext by context {

        override val screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)

        init {
            launch {
                val data: StatisticData = withContext(dispatchers.io) {
                    resultStore.loadStatistic()
                }
                if (data.totalAll == 0L) {
                    screenState.emit(ScreenState.Empty)
                } else {
                    val mastery: FactMasterySnapshot = withContext(dispatchers.io) {
                        val allStats: List<by.tigre.numbers.entity.FactStats> =
                            FactKey.Op.entries.flatMap { op -> factStore.getByOp(op) }
                        FactMasterySnapshot.compute(allStats, System.currentTimeMillis())
                    }
                    val types: List<GameType> = data.byType.keys.toList().sortedBy { it.ordinal }
                    screenState.emit(ScreenState.Data(data, types, mastery))
                }
            }
        }

        override fun onCloseClicked() = onClose()
    }
}

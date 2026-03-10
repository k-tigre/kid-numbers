package by.tigre.numbers.presentation.statistic

import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.StatisticData
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface StatisticComponent {

    val screenState: StateFlow<ScreenState>

    fun onCloseClicked()

    sealed interface ScreenState {
        data object Loading : ScreenState
        data object Empty : ScreenState
        data class Data(val statistic: StatisticData, val gameTypes: List<GameType>) : ScreenState
    }

    class Impl(
        context: BaseComponentContext,
        private val resultStore: ResultStore,
        private val onClose: () -> Unit
    ) : StatisticComponent, BaseComponentContext by context {

        override val screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)

        init {
            launch {
                val data = resultStore.loadStatistic()
                if (data.totalAll == 0L) {
                    screenState.emit(ScreenState.Empty)
                } else {
                    val types = data.byType.keys.toList().sortedBy { it.ordinal }
                    screenState.emit(ScreenState.Data(data, types))
                }
            }
        }

        override fun onCloseClicked() = onClose()
    }
}

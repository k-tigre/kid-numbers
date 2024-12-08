package by.tigre.numbers.presentation.history

import by.tigre.numbers.data.ResultStore
import by.tigre.numbers.entity.HistoryGameResult
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

interface HistoryComponent {

    val results: StateFlow<ScreenState>
    fun onCloseClicked()

    class Impl(
        context: BaseComponentContext,
        resultStore: ResultStore,
        private val onClose: () -> Unit
    ) : HistoryComponent, BaseComponentContext by context {

        override val results: StateFlow<ScreenState> = flow {
            val history = resultStore.load()
            emit(
                if (history.isNotEmpty()) {
                    ScreenState.History(history)
                } else {
                    ScreenState.Empty
                }
            )
        }
            .stateIn(this, SharingStarted.WhileSubscribed(), ScreenState.Loading)

        override fun onCloseClicked() {
            onClose()
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data object Empty : ScreenState
        data class History(val items: List<HistoryGameResult>) : ScreenState
    }
}

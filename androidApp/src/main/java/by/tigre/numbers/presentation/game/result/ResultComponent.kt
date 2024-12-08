package by.tigre.numbers.presentation.game.result

import by.tigre.numbers.data.ResultStore
import by.tigre.numbers.entity.GameResult
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ResultComponent {

    val results: StateFlow<GameResult>

    fun onClose()

    class Impl(
        context: BaseComponentContext,
        result: GameResult,
        resultStore: ResultStore,
        private val onFinish: () -> Unit
    ) : ResultComponent, BaseComponentContext by context {

        override val results: StateFlow<GameResult> = MutableStateFlow(result)

        override fun onClose() = onFinish()

        init {
            resultStore.save(result)
        }
    }

}

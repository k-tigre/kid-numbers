package by.tigre.numbers.presentation.game.result

import by.tigre.numbers.entity.GameResult
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MultiplicationResultComponent {

    val results: StateFlow<GameResult>

    fun onClose()

    class Impl(
        context: BaseComponentContext,
        result: GameResult,
        private val onFinish: () -> Unit
    ) : MultiplicationResultComponent, BaseComponentContext by context {

        override val results: StateFlow<GameResult> = MutableStateFlow(result.copy())

        override fun onClose() = onFinish()
    }

}

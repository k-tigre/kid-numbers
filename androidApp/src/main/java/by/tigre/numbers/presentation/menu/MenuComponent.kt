package by.tigre.numbers.presentation.menu

import by.tigre.numbers.entity.GameType
import by.tigre.tools.presentation.base.BaseComponentContext

interface MenuComponent {
    fun onMultiplicationClicked()
    fun onAdditionClicked()
    fun onDivisionClicked()
    fun onSubtractionClicked()
    fun onHistoryClicked()

    class Impl(
        context: BaseComponentContext,
        private val onGameTypeSelected: (GameType) -> Unit,
        private val onShowHistory: () -> Unit
    ) : MenuComponent, BaseComponentContext by context {
        override fun onMultiplicationClicked() {
            onGameTypeSelected(GameType.Multiplication)
        }

        override fun onAdditionClicked() {
            onGameTypeSelected(GameType.Additional)
        }

        override fun onDivisionClicked() {
            onGameTypeSelected(GameType.Division)
        }

        override fun onSubtractionClicked() {
            onGameTypeSelected(GameType.Subtraction)
        }

        override fun onHistoryClicked() {
            onShowHistory()
        }
    }
}

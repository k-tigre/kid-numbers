package by.tigre.numbers.presentation.menu

import by.tigre.numbers.entity.GameType
import by.tigre.tools.presentation.base.BaseComponentContext

interface MenuComponent {
    fun onMultiplicationClicked()
    fun onAdditionClicked()

    class Impl(
        context: BaseComponentContext,
        private val onGameTypeSelected: (GameType) -> Unit
    ) : MenuComponent, BaseComponentContext by context {
        override fun onMultiplicationClicked() {
            onGameTypeSelected(GameType.Multiplication)
        }

        override fun onAdditionClicked() {
            onGameTypeSelected(GameType.Additional)
        }
    }
}

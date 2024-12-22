package by.tigre.numbers.presentation.menu

import by.tigre.numbers.entity.GameType
import by.tigre.tools.presentation.base.BaseComponentContext

interface MenuComponent {
    val gameTypes: List<GameType>

    fun onGameClicked(type: GameType)
    fun onHistoryClicked()

    class Impl(
        context: BaseComponentContext,
        private val onGameTypeSelected: (GameType) -> Unit,
        private val onShowHistory: () -> Unit
    ) : MenuComponent, BaseComponentContext by context {
        override val gameTypes: List<GameType> = GameType.entries

        override fun onGameClicked(type: GameType) {
            onGameTypeSelected(type)
        }

        override fun onHistoryClicked() {
            onShowHistory()
        }
    }
}

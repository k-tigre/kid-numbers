package by.tigre.numbers.presentation.menu

import by.tigre.tools.presentation.base.BaseComponentContext

interface MenuComponent {
    fun onMultiplicationClicked()
    fun onAdditionClicked()

    class Impl(
        context: BaseComponentContext,
        private val navigator: MenuNavigator
    ) : MenuComponent, BaseComponentContext by context {
        override fun onMultiplicationClicked() {
            navigator.showMultiplicationScreen()
        }

        override fun onAdditionClicked() {
            navigator.showAdditionScreen()
        }
    }
}

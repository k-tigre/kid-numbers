package by.tigre.numbers.presentation.game.settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateMap
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface MultiplicationSettingsComponent {
    val isPositive: Boolean
    val numbersForSelection: StateFlow<List<Pair<Int, Boolean>>>
    val difficultSelection: StateFlow<Difficult>
    val isStartEnabled: StateFlow<Boolean>
    fun onNumberSelectionChanged(number: Int, isSelected: Boolean)
    fun onDifficultChanged(difficult: Difficult)
    fun onStartGameClicked()
    fun onBackClicked()

    @Immutable
    class Impl(
        context: BaseComponentContext,
        override val isPositive: Boolean,
        private val onStartGame: (GameSettings) -> Unit,
        private val onClose: () -> Unit
    ) : MultiplicationSettingsComponent, BaseComponentContext by context {
        private val numbers: MutableMap<Int, Boolean> = (1..9).map { it to false }.toMutableStateMap()

        override val numbersForSelection = MutableStateFlow(getState())
        override val difficultSelection = MutableStateFlow(Difficult.Medium)

        override val isStartEnabled = numbersForSelection.map { it.any { (_, isSelected) -> isSelected } }
            .stateIn(this, SharingStarted.Lazily, false)

        override fun onNumberSelectionChanged(number: Int, isSelected: Boolean) {
            numbers[number] = isSelected
            numbersForSelection.tryEmit(getState())
        }

        override fun onDifficultChanged(difficult: Difficult) {
            difficultSelection.tryEmit(difficult)
        }

        override fun onStartGameClicked() {
            onStartGame(
                GameSettings.Multiplication(
                    selectedNumbers = numbers.mapNotNull { if (it.value) it.key else null },
                    difficult = difficultSelection.value,
                    isPositive = isPositive
                )
            )
        }

        override fun onBackClicked() {
            onClose()
        }

        private fun getState() = numbers.map { it.key to it.value }
    }

}

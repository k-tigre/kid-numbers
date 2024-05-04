package by.tigre.numbers.presentation.multiplication.component

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateMap
import by.tigre.numbers.presentation.multiplication.GameSettings
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface MultiplicationSettingsComponent {
    val numbersForSelection: StateFlow<List<Pair<Int, Boolean>>>
    val difficultSelection: StateFlow<GameSettings.Difficult>
    val isStartEnabled: StateFlow<Boolean>
    fun onNumberSelectionChanged(number: Int, isSelected: Boolean)
    fun onDifficultChanged(difficult: GameSettings.Difficult)
    fun onStartGameClicked()

    @Immutable
    class Impl(
        context: BaseComponentContext,
        private val onStartGame: (GameSettings) -> Unit
    ) : MultiplicationSettingsComponent, BaseComponentContext by context {
        private val numbers: MutableMap<Int, Boolean> = (1..9).map { it to false }.toMutableStateMap()

        override val numbersForSelection = MutableStateFlow(getState())
        override val difficultSelection = MutableStateFlow(GameSettings.Difficult.Medium)

        override val isStartEnabled = numbersForSelection.map { it.any { (_, isSelected) -> isSelected } }
            .stateIn(this, SharingStarted.Lazily, false)

        override fun onNumberSelectionChanged(number: Int, isSelected: Boolean) {
            numbers[number] = isSelected
            numbersForSelection.tryEmit(getState())
        }

        override fun onDifficultChanged(difficult: GameSettings.Difficult) {
            difficultSelection.tryEmit(difficult)
        }

        override fun onStartGameClicked() {
            onStartGame(
                GameSettings(
                    selectedNumbers = numbers.mapNotNull { if (it.value) it.key else null },
                    difficult = difficultSelection.value
                )
            )
        }

        private fun getState() = numbers.map { it.key to it.value }
    }

}

package by.tigre.numbers.presentation.game.settings

import androidx.compose.runtime.Immutable
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Additional
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface AdditionalSettingsComponent {
    val isPositive: Boolean
    val numbersForSelection: StateFlow<List<Pair<GameSettings.Range, Boolean>>>
    val difficultSelection: StateFlow<Difficult>
    val isStartEnabled: StateFlow<Boolean>
    fun onNumberTypeSelectionChanged(range: GameSettings.Range, isSelected: Boolean)
    fun onDifficultChanged(difficult: Difficult)
    fun onStartGameClicked()
    fun onBackClicked()

    @Immutable
    class Impl(
        context: BaseComponentContext,
        override val isPositive: Boolean,
        private val onStartGame: (GameSettings) -> Unit,
        private val onClose: () -> Unit
    ) : AdditionalSettingsComponent, BaseComponentContext by context {
        private val numbers: MutableMap<GameSettings.Range, Boolean> = mutableMapOf(
            GameSettings.Range(10, false) to false,
            GameSettings.Range(100, false) to false,
            GameSettings.Range(1000, false) to false,
            GameSettings.Range(10, true) to false,
            GameSettings.Range(100, true) to false,
            GameSettings.Range(1000, true) to false,
        )

        override val numbersForSelection = MutableStateFlow(getState())
        override val difficultSelection = MutableStateFlow(Difficult.Medium)

        override val isStartEnabled = numbersForSelection.map { it.any { (_, isSelected) -> isSelected } }
            .stateIn(this, SharingStarted.Lazily, false)

        override fun onNumberTypeSelectionChanged(range: GameSettings.Range, isSelected: Boolean) {
            numbers[range] = isSelected
            numbersForSelection.tryEmit(getState())
        }

        override fun onDifficultChanged(difficult: Difficult) {
            difficultSelection.tryEmit(difficult)
        }

        override fun onStartGameClicked() {
            onStartGame(
                Additional(
                    ranges = numbers.mapNotNull { if (it.value) it.key else null },
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

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
    val numbersForSelection: StateFlow<List<Pair<Additional.Range, Boolean>>>
    val difficultSelection: StateFlow<Difficult>
    val isStartEnabled: StateFlow<Boolean>
    fun onNumberTypeSelectionChanged(range: Additional.Range, isSelected: Boolean)
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
        private val numbers: MutableMap<Additional.Range, Boolean> = mutableMapOf(
            Additional.Range(0, 10) to false,
            Additional.Range(0, 100) to false,
            Additional.Range(0, 500) to false,
            Additional.Range(0, 1000) to false,
            Additional.Range(100, 200) to false,
            Additional.Range(100, 500) to false,
            Additional.Range(100, 1000) to false,
            Additional.Range(100, 2000) to false,
        )

        override val numbersForSelection = MutableStateFlow(getState())
        override val difficultSelection = MutableStateFlow(Difficult.Medium)

        override val isStartEnabled = numbersForSelection.map { it.any { (_, isSelected) -> isSelected } }
            .stateIn(this, SharingStarted.Lazily, false)

        override fun onNumberTypeSelectionChanged(range: Additional.Range, isSelected: Boolean) {
            numbers[range] = isSelected
            numbersForSelection.tryEmit(getState())
        }

        override fun onDifficultChanged(difficult: Difficult) {
            difficultSelection.tryEmit(difficult)
        }

        override fun onStartGameClicked() {
            onStartGame(
                Additional(
                    type = numbers.mapNotNull { if (it.value) it.key else null },
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

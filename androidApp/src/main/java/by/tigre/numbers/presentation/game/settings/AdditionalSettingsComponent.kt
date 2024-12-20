package by.tigre.numbers.presentation.game.settings

import androidx.compose.runtime.Immutable
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.NumberType
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface AdditionalSettingsComponent {
    val isPositive: Boolean
    val numbersForSelection: StateFlow<List<Pair<NumberType, Boolean>>>
    val difficultSelection: StateFlow<Difficult>
    val isStartEnabled: StateFlow<Boolean>
    fun onNumberTypeSelectionChanged(type: NumberType, isSelected: Boolean)
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
        private val numbers: MutableMap<NumberType, Boolean> = mutableMapOf(
            NumberType.Single to false,
            NumberType.Double to false,
            NumberType.Triples to false,
            NumberType.SingleDouble to false,
            NumberType.SingleDoubleTriples to false,
        )

        override val numbersForSelection = MutableStateFlow(getState())
        override val difficultSelection = MutableStateFlow(Difficult.Medium)

        override val isStartEnabled = numbersForSelection.map { it.any { (_, isSelected) -> isSelected } }
            .stateIn(this, SharingStarted.Lazily, false)

        override fun onNumberTypeSelectionChanged(type: NumberType, isSelected: Boolean) {
            numbers[type] = isSelected
            numbersForSelection.tryEmit(getState())
        }

        override fun onDifficultChanged(difficult: Difficult) {
            difficultSelection.tryEmit(difficult)
        }

        override fun onStartGameClicked() {
            onStartGame(
                GameSettings.Additional(
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

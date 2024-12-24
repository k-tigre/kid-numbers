package by.tigre.numbers.presentation.game.settings

import androidx.compose.runtime.Immutable
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface EquationsSettingsComponent {
    val onScrollPosition: Flow<Int>
    val settings: StateFlow<Settings>

    fun onDifficultSelected(value: Difficult)
    fun onTypeSelected(value: Equations.Type)
    fun onRangeSelected(value: Equations.Range)
    fun onDimensionSelected(value: Equations.Dimension)
    fun onStartGameClicked()
    fun onBackClicked()

    data class Settings(
        val difficult: DifficultSection,
        val range: RangeSection,
        val type: TypeSection,
        val dimension: DimensionSection,
    ) {
        data class DifficultSection(
            val current: Difficult?,
            val values: List<Difficult>,
            val index: Int,
        )

        data class RangeSection(
            val current: Equations.Range?,
            val values: List<Equations.Range>,
            val index: Int,
        )

        data class TypeSection(
            val current: Equations.Type?,
            val values: List<Equations.Type>,
            val index: Int,
        )

        data class DimensionSection(
            val current: Equations.Dimension?,
            val values: List<Equations.Dimension>,
            val index: Int,
        )

        companion object {
            private val DIFFICULT = Difficult.entries
            private val RANGES =
                listOf(Equations.Range(-20, 20), Equations.Range(-50, 50), Equations.Range(-100, 100), Equations.Range(-1000, 1000))
            private val TYPES = Equations.Type.entries
            private val DIMENSIONS = Equations.Dimension.entries

            val DEFAULTS: Settings
                get() {
                    val difficult = DifficultSection(
                        current = Difficult.Medium,
                        values = DIFFICULT,
                        index = 0,
                    )

                    val range = RangeSection(
                        current = null,
                        values = RANGES,
                        index = difficult.index + 1 + difficult.values.size
                    )

                    val type = TypeSection(
                        current = null,
                        values = TYPES,
                        index = range.index + 1 + range.values.size
                    )

                    val dimension = DimensionSection(
                        current = Equations.Dimension.Single, // TODO null
                        values = DIMENSIONS,
                        index = type.index + 1 + type.values.size
                    )

                    return Settings(
                        difficult = difficult,
                        range = range,
                        type = type,
                        dimension = dimension
                    )
                }
        }
    }

    @Immutable
    class Impl(
        context: BaseComponentContext,
        private val onStartGame: (GameSettings) -> Unit,
        private val onClose: () -> Unit
    ) : EquationsSettingsComponent, BaseComponentContext by context {

        override val settings = MutableStateFlow(Settings.DEFAULTS)

        override val onScrollPosition = MutableSharedFlow<Int>()

        override fun onDifficultSelected(value: Difficult) {
            launch {
                val current = settings.value
                settings.emit(current.copy(difficult = current.difficult.copy(current = value)))
            }
        }

        override fun onTypeSelected(value: Equations.Type) {
            launch {
                val current = settings.value
                settings.emit(current.copy(type = current.type.copy(current = value)))
            }
        }

        override fun onRangeSelected(value: Equations.Range) {
            launch {
                val current = settings.value
                settings.emit(current.copy(range = current.range.copy(current = value)))
            }
        }

        override fun onDimensionSelected(value: Equations.Dimension) {
            launch {
                val current = settings.value
                settings.emit(current.copy(dimension = current.dimension.copy(current = value)))
            }
        }

        override fun onStartGameClicked() {
            launch {
                val settings = settings.value

                when {
                    settings.difficult.current == null -> onScrollPosition.emit(settings.difficult.index)
                    settings.range.current == null -> onScrollPosition.emit(settings.range.index)
                    settings.type.current == null -> onScrollPosition.emit(settings.type.index)
                    settings.dimension.current == null -> onScrollPosition.emit(settings.dimension.index)
                    else -> {
                        onStartGame(
                            Equations(
                                ranges = settings.range.current,
                                difficult = settings.difficult.current,
                                type = settings.type.current,
                                dimension = settings.dimension.current
                            )
                        )
                    }
                }
            }
        }

        override fun onBackClicked() {
            onClose()
        }
    }
}

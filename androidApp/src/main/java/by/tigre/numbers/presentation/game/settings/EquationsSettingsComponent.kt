package by.tigre.numbers.presentation.game.settings

import androidx.compose.runtime.Immutable
import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.settings.SettingsUtils.DifficultSection
import by.tigre.numbers.presentation.game.settings.SettingsUtils.DimensionSection
import by.tigre.numbers.presentation.game.settings.SettingsUtils.RangeSection
import by.tigre.numbers.presentation.game.settings.SettingsUtils.TypeSection
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

interface EquationsSettingsComponent {
    val onScrollPosition: Flow<Int>
    val settings: StateFlow<Settings>

    fun onDifficultSelected(value: Difficult)
    fun onTypeSelected(value: Equations.Type)
    fun onRangeSelected(value: GameSettings.Range)
    fun onDimensionSelected(value: Equations.Dimension)
    fun onStartGameClicked()
    fun onBackClicked()

    data class Settings(
        val difficult: DifficultSection,
        val range: RangeSection,
        val type: TypeSection,
        val dimension: DimensionSection,
    ) {
        companion object {
            private val DIFFICULT = Difficult.entries
            private val RANGES = listOf(
                GameSettings.Range(50, false),
                GameSettings.Range(100, false),
                GameSettings.Range(20, true),
                GameSettings.Range(50, true),
                GameSettings.Range(100, true),
                GameSettings.Range(1000, true)
            )
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
        analytics: EventAnalytics,
        private val onStartGame: (GameSettings) -> Unit,
        private val onClose: () -> Unit
    ) : EquationsSettingsComponent, BaseComponentContext by context {
        private val onScrollPositionInternal = MutableSharedFlow<Int>()
        override val settings = MutableStateFlow(Settings.DEFAULTS)

        override val onScrollPosition = onScrollPositionInternal
            .onEach { analytics.trackEvent(Event.Action.UI.SettingScroll(GameType.Equations)) }
            .shareIn(this, started = SharingStarted.WhileSubscribed())

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

        override fun onRangeSelected(value: GameSettings.Range) {
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
                    settings.difficult.current == null -> onScrollPositionInternal.emit(settings.difficult.index)
                    settings.range.current == null -> onScrollPositionInternal.emit(settings.range.index)
                    settings.type.current == null -> onScrollPositionInternal.emit(settings.type.index)
                    settings.dimension.current == null -> onScrollPositionInternal.emit(settings.dimension.index)
                    else -> {
                        onStartGame(
                            Equations(
                                range = settings.range.current,
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

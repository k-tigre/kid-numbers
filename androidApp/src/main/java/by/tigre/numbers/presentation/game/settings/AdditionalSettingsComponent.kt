package by.tigre.numbers.presentation.game.settings

import androidx.compose.runtime.Immutable
import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Additional
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.settings.SettingsUtils.DifficultSection
import by.tigre.numbers.presentation.game.settings.SettingsUtils.RangeSection
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface AdditionalSettingsComponent {
    val isPositive: Boolean
    val onScrollPosition: Flow<Int>
    val settings: StateFlow<Settings>

    fun onRangeSelected(value: GameSettings.Range)
    fun onDifficultSelected(value: Difficult)
    fun onStartGameClicked()
    fun onBackClicked()

    data class Settings(
        val difficult: DifficultSection,
        val range: RangeSection,
    ) {

        companion object {
            private val DIFFICULT = Difficult.entries
            private val RANGES = listOf(
                GameSettings.Range(10, false),
                GameSettings.Range(100, false),
                GameSettings.Range(500, false),
                GameSettings.Range(1000, false),
            )

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

                    return Settings(
                        difficult = difficult,
                        range = range,
                    )
                }
        }
    }

    @Immutable
    class Impl(
        context: BaseComponentContext,
        override val isPositive: Boolean,
        analytics: EventAnalytics,
        private val onStartGame: (GameSettings) -> Unit,
        private val onClose: () -> Unit
    ) : AdditionalSettingsComponent, BaseComponentContext by context {
        override val settings = MutableStateFlow(Settings.DEFAULTS)

        private val onScrollPositionInternal = MutableSharedFlow<Int>()
        override val onScrollPosition = onScrollPositionInternal
            .onEach { analytics.trackEvent(Event.Action.UI.SettingScroll(GameType.Equations)) }

        override fun onDifficultSelected(value: Difficult) {
            launch {
                val current = settings.value
                settings.emit(current.copy(difficult = current.difficult.copy(current = value)))
            }
        }

        override fun onRangeSelected(value: GameSettings.Range) {
            launch {
                val current = settings.value
                settings.emit(current.copy(range = current.range.copy(current = value)))
            }
        }

        override fun onStartGameClicked() {
            launch {
                val settings = settings.value

                when {
                    settings.difficult.current == null -> onScrollPositionInternal.emit(settings.difficult.index)
                    settings.range.current == null -> onScrollPositionInternal.emit(settings.range.index)
                    else -> {
                        onStartGame(
                            Additional(
                                range = settings.range.current,
                                difficult = settings.difficult.current,
                                isPositive = isPositive
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

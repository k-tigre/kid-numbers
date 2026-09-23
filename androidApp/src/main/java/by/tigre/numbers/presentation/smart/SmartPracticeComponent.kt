package by.tigre.numbers.presentation.smart

import by.tigre.numbers.data.facts.SmartPracticePreferences
import by.tigre.numbers.data.remoteconfig.FeatureFlags
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.SmartPracticeSettings
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface SmartPracticeComponent {
    val visibleOps: List<FactKey.Op>
    val selectedOps: StateFlow<Set<FactKey.Op>>
    val isStartEnabled: StateFlow<Boolean>

    fun onOpToggled(op: FactKey.Op)
    fun onStart()
    fun onBack()

    interface Router {
        fun startSmartGame(settings: SmartPracticeSettings)
        fun close()
    }

    class Impl(
        context: BaseComponentContext,
        featureFlags: FeatureFlags,
        private val smartPracticePreferences: SmartPracticePreferences,
        private val router: Router,
    ) : SmartPracticeComponent, BaseComponentContext by context {

        override val visibleOps: List<FactKey.Op> = OP_ORDER.filter { it in featureFlags.smartOpsEnabled() }
        private val selectedOpsState: MutableStateFlow<Set<FactKey.Op>> = MutableStateFlow(loadSelectedOps())
        override val selectedOps: StateFlow<Set<FactKey.Op>> = selectedOpsState
        override val isStartEnabled: StateFlow<Boolean> = selectedOpsState
            .map { it.isNotEmpty() }
            .stateIn(this, SharingStarted.WhileSubscribed(), selectedOpsState.value.isNotEmpty())

        override fun onOpToggled(op: FactKey.Op) {
            if (op !in visibleOps) return
            val current: Set<FactKey.Op> = selectedOpsState.value
            selectedOpsState.value = if (op in current) current - op else current + op
        }

        override fun onStart() {
            val selected: Set<FactKey.Op> = selectedOpsState.value
            if (selected.isEmpty()) return
            smartPracticePreferences.saveEnabledOps(selected)
            router.startSmartGame(SmartPracticeSettings(enabledOps = selected))
        }

        override fun onBack() {
            router.close()
        }

        private fun loadSelectedOps(): Set<FactKey.Op> {
            val visible: Set<FactKey.Op> = visibleOps.toSet()
            return smartPracticePreferences.loadEnabledOps(default = visible).intersect(visible)
        }

        private companion object {
            val OP_ORDER: List<FactKey.Op> = listOf(
                FactKey.Op.ADD,
                FactKey.Op.SUB,
                FactKey.Op.MUL,
                FactKey.Op.DIV,
            )
        }
    }
}

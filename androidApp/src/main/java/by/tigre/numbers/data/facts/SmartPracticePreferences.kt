package by.tigre.numbers.data.facts

import by.tigre.numbers.data.storage.Preferences
import by.tigre.numbers.entity.FactKey

interface SmartPracticePreferences {
    fun loadEnabledOps(default: Set<FactKey.Op>): Set<FactKey.Op>
    fun saveEnabledOps(ops: Set<FactKey.Op>)
}

class SmartPracticePreferencesImpl(
    private val preferences: Preferences,
) : SmartPracticePreferences {

    override fun loadEnabledOps(default: Set<FactKey.Op>): Set<FactKey.Op> {
        val raw: String = preferences.loadString(KEY_ENABLED_OPS, default = "")
        if (raw.isBlank()) return default
        return parseOpsCsv(raw, fallback = default)
    }

    override fun saveEnabledOps(ops: Set<FactKey.Op>) {
        preferences.saveString(KEY_ENABLED_OPS, ops.joinToString(",") { it.name })
    }

    private companion object {
        const val KEY_ENABLED_OPS: String = "smart_practice_enabled_ops"
    }
}

internal fun parseOpsCsv(raw: String, fallback: Set<FactKey.Op>): Set<FactKey.Op> {
    val parsed: Set<FactKey.Op> = raw.split(",")
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { token -> runCatching { FactKey.Op.valueOf(token.uppercase()) }.getOrNull() }
        .toSet()
    return parsed.ifEmpty { fallback }
}

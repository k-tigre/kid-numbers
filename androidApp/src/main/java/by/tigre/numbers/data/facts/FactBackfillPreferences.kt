package by.tigre.numbers.data.facts

import by.tigre.numbers.data.storage.Preferences

interface FactBackfillPreferences {
    fun isDone(): Boolean
    fun markDone()
}

class FactBackfillPreferencesImpl(
    private val preferences: Preferences,
) : FactBackfillPreferences {

    override fun isDone(): Boolean = preferences.loadBoolean(KEY_DONE, default = false)

    override fun markDone() {
        preferences.saveBoolean(KEY_DONE, value = true)
    }

    private companion object {
        const val KEY_DONE: String = "facts_backfill_v1_done"
    }
}

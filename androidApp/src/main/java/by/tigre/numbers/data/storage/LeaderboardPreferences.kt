package by.tigre.numbers.data.storage

import java.util.UUID

interface LeaderboardPreferences {
    fun loadNickname(default: String): String
    fun saveNickname(value: String)
    fun getOrCreateUserId(): String
    fun isBackfillDone(): Boolean
    fun markBackfillDone()
}

class LeaderboardPreferencesImpl(
    private val preferences: Preferences,
) : LeaderboardPreferences {

    override fun loadNickname(default: String): String {
        return preferences.loadString(KEY_NICKNAME, default = default).trim().ifBlank { default }
    }

    override fun saveNickname(value: String) {
        preferences.saveString(KEY_NICKNAME, value.trim())
    }

    override fun getOrCreateUserId(): String {
        val existing: String = preferences.loadString(KEY_USER_ID, default = "").trim()
        if (existing.isNotEmpty()) return existing
        val created: String = UUID.randomUUID().toString()
        preferences.saveString(KEY_USER_ID, created)
        return created
    }

    override fun isBackfillDone(): Boolean = preferences.loadBoolean(KEY_BACKFILL_DONE, default = false)

    override fun markBackfillDone() {
        preferences.saveBoolean(KEY_BACKFILL_DONE, value = true)
    }

    private companion object {
        const val KEY_NICKNAME: String = "leaderboard_nickname"
        const val KEY_USER_ID: String = "leaderboard_user_id"
        const val KEY_BACKFILL_DONE: String = "leaderboard_user_id_backfill_done"
    }
}

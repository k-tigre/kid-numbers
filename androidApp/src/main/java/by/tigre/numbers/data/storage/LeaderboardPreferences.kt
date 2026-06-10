package by.tigre.numbers.data.storage

import by.tigre.numbers.entity.GameSettings
import java.util.UUID
import kotlinx.serialization.json.Json

interface LeaderboardPreferences {
    fun loadNickname(default: String): String
    fun saveNickname(value: String)
    fun isNicknameConfigured(): Boolean
    fun getOrCreateUserId(): String
    fun isBackfillDone(): Boolean
    fun markBackfillDone()
    fun loadLastBoardKey(): String?
    fun saveLastBoardKey(boardKey: String)
    fun loadLastBoardSettings(): GameSettings?
    fun saveLastBoardSettings(settings: GameSettings)
}

class LeaderboardPreferencesImpl(
    private val preferences: Preferences,
) : LeaderboardPreferences {

    override fun loadNickname(default: String): String {
        return preferences.loadString(KEY_NICKNAME, default = default).trim().ifBlank { default }
    }

    override fun saveNickname(value: String) {
        val trimmed: String = value.trim()
        preferences.saveString(KEY_NICKNAME, trimmed)
        if (trimmed.isNotEmpty()) {
            preferences.saveBoolean(KEY_NICKNAME_CONFIGURED, value = true)
        }
    }

    override fun isNicknameConfigured(): Boolean {
        if (preferences.loadBoolean(KEY_NICKNAME_CONFIGURED, default = false)) return true
        val saved: String = preferences.loadString(KEY_NICKNAME, default = "").trim()
        if (saved.isNotEmpty()) {
            preferences.saveBoolean(KEY_NICKNAME_CONFIGURED, value = true)
            return true
        }
        return false
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

    override fun loadLastBoardKey(): String? {
        return preferences.loadString(KEY_LAST_BOARD_KEY, default = "").trim().ifBlank { null }
    }

    override fun saveLastBoardKey(boardKey: String) {
        preferences.saveString(KEY_LAST_BOARD_KEY, boardKey)
    }

    override fun loadLastBoardSettings(): GameSettings? {
        val raw: String = preferences.loadString(KEY_LAST_BOARD_SETTINGS, default = "").trim()
        if (raw.isEmpty()) return null
        return runCatching {
            settingsJson.decodeFromString(GameSettings.serializer(), raw)
        }.getOrNull()
    }

    override fun saveLastBoardSettings(settings: GameSettings) {
        preferences.saveString(KEY_LAST_BOARD_SETTINGS, settingsJson.encodeToString(GameSettings.serializer(), settings))
    }

    private companion object {
        val settingsJson: Json = Json { ignoreUnknownKeys = true }
        const val KEY_NICKNAME: String = "leaderboard_nickname"
        const val KEY_NICKNAME_CONFIGURED: String = "leaderboard_nickname_configured"
        const val KEY_USER_ID: String = "leaderboard_user_id"
        const val KEY_BACKFILL_DONE: String = "leaderboard_user_id_backfill_done"
        const val KEY_LAST_BOARD_KEY: String = "leaderboard_last_board_key"
        const val KEY_LAST_BOARD_SETTINGS: String = "leaderboard_last_board_settings"
    }
}

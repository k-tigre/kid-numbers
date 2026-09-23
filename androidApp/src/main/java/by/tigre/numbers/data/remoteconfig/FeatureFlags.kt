package by.tigre.numbers.data.remoteconfig

import by.tigre.numbers.data.facts.parseOpsCsv
import by.tigre.numbers.domain.GameDurationProvider
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface FeatureFlags {
    val isLeaderboardEnabled: StateFlow<Boolean>
    val isPurchasesEnabled: StateFlow<Boolean>
    val isEquationsDimensionEnabled: StateFlow<Boolean>
    val isSmartPracticeEnabled: StateFlow<Boolean>
    fun smartOpsEnabled(): Set<FactKey.Op>
    fun calculateLeaderboardRating(settings: GameSettings, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int
    fun calculateLeaderboardRating(difficult: Difficult, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int
    fun timeCapSeconds(settings: GameSettings): Int
    suspend fun refreshRemoteConfig(): Boolean
}

class FeatureFlagsImpl(
    private val provider: RemoteConfigProvider,
    private val gameDurationProvider: GameDurationProvider,
) : FeatureFlags {

    private val _isLeaderboardEnabled: MutableStateFlow<Boolean> = MutableStateFlow(readLeaderboardEnabled())
    override val isLeaderboardEnabled: StateFlow<Boolean> = _isLeaderboardEnabled.asStateFlow()
    private val _isPurchasesEnabled: MutableStateFlow<Boolean> = MutableStateFlow(readPurchasesEnabled())
    override val isPurchasesEnabled: StateFlow<Boolean> = _isPurchasesEnabled.asStateFlow()
    private val _isEquationsDimensionEnabled: MutableStateFlow<Boolean> = MutableStateFlow(readEquationsDimensionEnabled())
    override val isEquationsDimensionEnabled: StateFlow<Boolean> = _isEquationsDimensionEnabled.asStateFlow()
    private val _isSmartPracticeEnabled: MutableStateFlow<Boolean> = MutableStateFlow(readSmartPracticeEnabled())
    override val isSmartPracticeEnabled: StateFlow<Boolean> = _isSmartPracticeEnabled.asStateFlow()

    override fun smartOpsEnabled(): Set<FactKey.Op> {
        val raw: String = provider.getString(RemoteConfigKeys.SMART_OPS_ENABLED, default = DEFAULT_SMART_OPS)
        return parseOpsCsv(raw, fallback = DEFAULT_SMART_OPS_SET)
    }

    override fun calculateLeaderboardRating(settings: GameSettings, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int {
        val json: String = provider.getString(RemoteConfigKeys.LEADERBOARD_RATING_JSON, default = "")
        return parseLeaderboardRatingConfig(json).calculate(
            timeCapSeconds = timeCapSeconds(settings),
            elapsedSeconds = elapsedSeconds,
            hintsUsed = hintsUsed,
            mistakes = mistakes,
        )
    }

    override fun calculateLeaderboardRating(difficult: Difficult, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int {
        val json: String = provider.getString(RemoteConfigKeys.LEADERBOARD_RATING_JSON, default = "")
        return parseLeaderboardRatingConfig(json).calculate(difficult, elapsedSeconds, hintsUsed, mistakes)
    }

    override fun timeCapSeconds(settings: GameSettings): Int {
        return (gameDurationProvider.provide(settings) / 1000L).toInt().coerceAtLeast(1)
    }

    override suspend fun refreshRemoteConfig(): Boolean {
        val updated: Boolean = provider.refresh()
        syncFromProvider()
        return updated
    }

    private fun syncFromProvider() {
        _isLeaderboardEnabled.value = readLeaderboardEnabled()
        _isPurchasesEnabled.value = readPurchasesEnabled()
        _isEquationsDimensionEnabled.value = readEquationsDimensionEnabled()
        _isSmartPracticeEnabled.value = readSmartPracticeEnabled()
    }

    private fun readLeaderboardEnabled(): Boolean {
        return provider.getBoolean(RemoteConfigKeys.LEADERBOARD_ENABLED, default = false)
    }

    private fun readPurchasesEnabled(): Boolean {
        return provider.getBoolean(RemoteConfigKeys.PURCHASES_ENABLED, default = false)
    }

    private fun readEquationsDimensionEnabled(): Boolean {
        return provider.getBoolean(RemoteConfigKeys.EQUATIONS_DIMENSION_ENABLED, default = false)
    }

    private fun readSmartPracticeEnabled(): Boolean {
        return provider.getBoolean(
            RemoteConfigKeys.SMART_PRACTICE_ENABLED,
            default = DEFAULT_SMART_PRACTICE_ENABLED,
        )
    }

    companion object {
        const val DEFAULT_SMART_PRACTICE_ENABLED: Boolean = true
        const val DEFAULT_SMART_OPS: String = "ADD,SUB,MUL,DIV"
        val DEFAULT_SMART_OPS_SET: Set<FactKey.Op> = setOf(
            FactKey.Op.ADD,
            FactKey.Op.SUB,
            FactKey.Op.MUL,
            FactKey.Op.DIV,
        )
    }
}

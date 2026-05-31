package by.tigre.numbers.data.remoteconfig

import by.tigre.numbers.entity.Difficult

interface FeatureFlags {
    fun calculateLeaderboardRating(difficult: Difficult, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int
    suspend fun refreshRemoteConfig(): Boolean
}

class FeatureFlagsImpl(
    private val provider: RemoteConfigProvider,
) : FeatureFlags {

    override fun calculateLeaderboardRating(difficult: Difficult, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int {
        val json: String = provider.getString(RemoteConfigKeys.LEADERBOARD_RATING_JSON, default = "")
        return parseLeaderboardRatingConfig(json).calculate(difficult, elapsedSeconds, hintsUsed, mistakes)
    }

    override suspend fun refreshRemoteConfig(): Boolean = provider.refresh()
}

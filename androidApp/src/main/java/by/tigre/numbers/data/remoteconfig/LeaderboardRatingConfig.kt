package by.tigre.numbers.data.remoteconfig

import by.tigre.numbers.entity.Difficult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LeaderboardRatingConfig(
    val hintPenalty: Int = DEFAULT.hintPenalty,
    val mistakePenalty: Int = DEFAULT.mistakePenalty,
    val minRating: Int = DEFAULT.minRating,
) {
    fun calculate(difficult: Difficult, elapsedSeconds: Long, hintsUsed: Int, mistakes: Int): Int {
        val timeCapSeconds: Int = (difficult.time / 1000L).toInt()
        val raw: Int = timeCapSeconds - elapsedSeconds.toInt() - hintsUsed * hintPenalty - mistakes * mistakePenalty
        return raw.coerceAtLeast(minRating)
    }

    companion object {
        val DEFAULT: LeaderboardRatingConfig = LeaderboardRatingConfig(
            hintPenalty = 0,
            mistakePenalty = 10,
            minRating = 1,
        )
        const val DEFAULT_JSON: String = """{"hintPenalty":0,"mistakePenalty":10,"minRating":1}"""
    }
}

private val ratingJson: Json = Json { ignoreUnknownKeys = true }

fun parseLeaderboardRatingConfig(json: String): LeaderboardRatingConfig {
    if (json.isBlank()) return LeaderboardRatingConfig.DEFAULT
    return runCatching {
        ratingJson.decodeFromString<LeaderboardRatingConfig>(json)
    }.getOrDefault(LeaderboardRatingConfig.DEFAULT)
}

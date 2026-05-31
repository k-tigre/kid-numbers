package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.remoteconfig.FeatureFlags
import by.tigre.numbers.data.storage.LeaderboardPreferences
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.HistoryGameResult
import by.tigre.numbers.entity.LeaderboardSubmitRequest

class LeaderboardHistoryBackfill(
    private val resultStore: ResultStore,
    private val leaderboardRepository: LeaderboardRepository,
    private val featureFlags: FeatureFlags,
    private val leaderboardPreferences: LeaderboardPreferences,
) {

    suspend fun runIfNeeded(defaultNickname: String): Result<Int> = runCatching {
        if (leaderboardPreferences.isBackfillDone()) return@runCatching 0
        val userId: String = leaderboardPreferences.getOrCreateUserId()
        val nickname: String = leaderboardPreferences.loadNickname(default = defaultNickname)
        val perfectGames: List<HistoryGameResult> = resultStore.load(
            difficult = Difficult.entries,
            types = GameType.entries,
            onlySuccess = true,
            limit = 10_000,
        ).sortedBy { it.date }
        var submittedCount: Int = 0
        perfectGames.forEach { entry ->
            val elapsedSeconds: Long = entry.duration / 1000L
            val mistakes: Int = entry.totalCount - entry.correctCount
            val gameScore: Int = featureFlags.calculateLeaderboardRating(
                difficult = entry.difficult,
                elapsedSeconds = elapsedSeconds,
                hintsUsed = 0,
                mistakes = mistakes,
            )
            leaderboardRepository.addGameScore(
                LeaderboardSubmitRequest(
                    userId = userId,
                    nickname = nickname,
                    gameScore = gameScore,
                    solveTimeSeconds = elapsedSeconds,
                    hintsUsed = 0,
                    mistakes = mistakes,
                    difficult = entry.difficult,
                    timestampMillis = entry.date,
                )
            ).onSuccess { submittedCount += 1 }
        }
        leaderboardPreferences.markBackfillDone()
        submittedCount
    }
}

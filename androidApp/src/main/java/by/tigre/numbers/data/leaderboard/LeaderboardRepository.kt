package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.numbers.entity.LeaderboardSubmitRequest

interface LeaderboardRepository {
    suspend fun addGameScore(request: LeaderboardSubmitRequest): Result<Unit>
    suspend fun fetchTopEntries(difficult: Difficult, limit: Int = 50): Result<List<LeaderboardEntry>>
}

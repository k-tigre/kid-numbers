package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.numbers.entity.LeaderboardSpeedEntry
import by.tigre.numbers.entity.LeaderboardSpeedSubmit
import by.tigre.numbers.entity.LeaderboardSubmitRequest

interface LeaderboardRepository {
    suspend fun submitSpeedRecord(request: LeaderboardSpeedSubmit): Result<Unit>
    suspend fun addTotalScore(request: LeaderboardSubmitRequest): Result<Unit>
    suspend fun fetchBestSpeedEntry(boardKey: String): Result<LeaderboardSpeedEntry?>
    suspend fun fetchTopSpeedEntries(boardKey: String, limit: Int = 50): Result<List<LeaderboardSpeedEntry>>
    suspend fun fetchTopTotalEntries(limit: Int = 50): Result<List<LeaderboardEntry>>
}

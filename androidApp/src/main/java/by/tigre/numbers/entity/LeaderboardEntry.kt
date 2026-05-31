package by.tigre.numbers.entity

data class LeaderboardEntry(
    val nickname: String,
    val totalScore: Int,
    val gamesCount: Int,
    val difficult: Difficult,
    val timestampMillis: Long,
)

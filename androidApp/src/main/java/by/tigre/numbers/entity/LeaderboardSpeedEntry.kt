package by.tigre.numbers.entity

data class LeaderboardSpeedEntry(
    val nickname: String,
    val bestTimeSeconds: Long,
    val mistakes: Int,
    val timestampMillis: Long,
)

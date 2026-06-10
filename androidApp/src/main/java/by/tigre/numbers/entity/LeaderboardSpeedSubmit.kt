package by.tigre.numbers.entity

data class LeaderboardSpeedSubmit(
    val userId: String,
    val nickname: String,
    val boardKey: String,
    val gameType: GameType,
    val difficult: Difficult,
    val solveTimeSeconds: Long,
    val mistakes: Int,
    val taskCount: Int,
    val timestampMillis: Long,
)

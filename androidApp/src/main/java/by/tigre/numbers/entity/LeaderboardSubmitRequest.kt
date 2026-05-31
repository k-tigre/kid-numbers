package by.tigre.numbers.entity

/** Points earned in a single perfect game; added to the player's cumulative total. */
data class LeaderboardSubmitRequest(
    val userId: String,
    val nickname: String,
    val gameScore: Int,
    val solveTimeSeconds: Long,
    val hintsUsed: Int,
    val mistakes: Int,
    val difficult: Difficult,
    val timestampMillis: Long,
)

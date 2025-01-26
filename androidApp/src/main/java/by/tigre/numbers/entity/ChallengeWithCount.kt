package by.tigre.numbers.entity

data class ChallengeWithCount(
    val id: String,
    val taskCount: Int,
    val startDate: Long,
    val status: Challenge.Status,
    val duration: Challenge.Duration
)
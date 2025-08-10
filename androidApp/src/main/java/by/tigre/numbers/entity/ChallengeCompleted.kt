package by.tigre.numbers.entity

data class ChallengeCompleted(
    val id: String,
    val taskCount: Int,
    val startTime: Long,
    val duration: Long,
    val isSuccess: Boolean
)
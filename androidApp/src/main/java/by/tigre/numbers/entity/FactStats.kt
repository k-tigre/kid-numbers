package by.tigre.numbers.entity

data class FactStats(
    val key: FactKey,
    val p: Float = 0.5f,
    val avgTimeMs: Int = 0,
    val attempts: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    val lastSeenMs: Long = 0L,
    val intervalDays: Int = 0,
    val dueAtMs: Long = 0L,
    val lastErrorType: String? = null,
)

package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactStats

object FactModelUpdater {
    private const val DAY_MS: Long = 24L * 60 * 60 * 1000

    fun adaptiveAlpha(attempts: Int): Float {
        return when {
            attempts < 5 -> 0.30f
            attempts < 20 -> 0.15f
            else -> 0.08f
        }
    }

    fun next(stats: FactStats, correct: Boolean, timeMs: Long?, nowMs: Long, errorType: String? = null): FactStats {
        val alpha: Float = adaptiveAlpha(stats.attempts)
        val target: Float = if (correct) 1f else 0f
        val newP: Float = (stats.p + alpha * (target - stats.p)).coerceIn(0.01f, 0.99f)
        val newAvgTimeMs: Int = when {
            !correct || timeMs == null -> stats.avgTimeMs
            stats.avgTimeMs == 0 -> timeMs.toInt()
            else -> (stats.avgTimeMs * 0.8 + timeMs * 0.2).toInt()
        }
        val newStreak: Int = if (correct) stats.streak + 1 else 0
        val newAttempts: Int = stats.attempts + 1
        val newCorrect: Int = if (correct) stats.correct + 1 else stats.correct
        val newIntervalDays: Int = when {
            !correct -> 1
            stats.intervalDays == 0 -> 1
            stats.intervalDays == 1 -> 3
            else -> (stats.intervalDays * 2).coerceAtMost(60)
        }
        val newDueAtMs: Long = nowMs + newIntervalDays * DAY_MS
        val newLastErrorType: String? = when {
            correct -> null
            errorType != null -> errorType
            else -> stats.lastErrorType
        }
        return stats.copy(
            p = newP,
            avgTimeMs = newAvgTimeMs,
            attempts = newAttempts,
            correct = newCorrect,
            streak = newStreak,
            lastSeenMs = nowMs,
            intervalDays = newIntervalDays,
            dueAtMs = newDueAtMs,
            lastErrorType = newLastErrorType,
        )
    }
}

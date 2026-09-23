package by.tigre.numbers.data.facts

import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.db.Fact
import by.tigre.numbers.domain.facts.FactModelUpdater
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats

interface FactStore {
    suspend fun get(key: FactKey): FactStats?
    suspend fun getByOp(op: FactKey.Op): List<FactStats>
    suspend fun getByOps(ops: Set<FactKey.Op>): List<FactStats>
    suspend fun countDue(ops: Set<FactKey.Op>, nowMs: Long): Int
    suspend fun upsert(stats: FactStats)
    suspend fun applyAnswer(key: FactKey, correct: Boolean, timeMs: Long?, nowMs: Long, errorType: String? = null)
    suspend fun ensureKeys(keys: Set<FactKey>)

    class Impl(
        private val database: DatabaseNumbers,
    ) : FactStore {
        override suspend fun get(key: FactKey): FactStats? {
            return database.factQueries.selectByKey(key.toStorageKey()).executeAsOneOrNull()?.toStats()
        }

        override suspend fun getByOp(op: FactKey.Op): List<FactStats> {
            return database.factQueries.selectByOp(op.name).executeAsList().map { it.toStats() }
        }

        override suspend fun getByOps(ops: Set<FactKey.Op>): List<FactStats> {
            return ops.flatMap { op -> getByOp(op) }
        }

        override suspend fun countDue(ops: Set<FactKey.Op>, nowMs: Long): Int {
            return countDueStats(getByOps(ops), nowMs)
        }

        override suspend fun upsert(stats: FactStats) {
            database.factQueries.upsertFact(
                key = stats.key.toStorageKey(),
                op = stats.key.op.name,
                a = stats.key.a,
                b = stats.key.b,
                p = stats.p,
                avgTimeMs = stats.avgTimeMs,
                attempts = stats.attempts,
                correct = stats.correct,
                streak = stats.streak,
                lastSeenMs = stats.lastSeenMs,
                intervalDays = stats.intervalDays,
                dueAtMs = stats.dueAtMs,
                lastErrorType = stats.lastErrorType,
            )
        }

        override suspend fun applyAnswer(key: FactKey, correct: Boolean, timeMs: Long?, nowMs: Long, errorType: String?) {
            val current: FactStats = get(key) ?: FactStats(key = key)
            upsert(FactModelUpdater.next(current, correct, timeMs, nowMs, errorType))
        }

        override suspend fun ensureKeys(keys: Set<FactKey>) {
            keys.forEach { key ->
                if (get(key) == null) {
                    upsert(FactStats(key = key))
                }
            }
        }

        private fun Fact.toStats(): FactStats {
            return FactStats(
                key = FactKey.parse(key),
                p = p,
                avgTimeMs = avgTimeMs,
                attempts = attempts,
                correct = correct,
                streak = streak,
                lastSeenMs = lastSeenMs,
                intervalDays = intervalDays,
                dueAtMs = dueAtMs,
                lastErrorType = lastErrorType,
            )
        }
    }
}

internal fun countDueStats(stats: List<FactStats>, nowMs: Long): Int {
    return stats.count { stat ->
        stat.attempts >= 1 && stat.dueAtMs > 0L && stat.dueAtMs <= nowMs
    }
}

package by.tigre.numbers.data.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import org.junit.Assert.assertEquals
import org.junit.Test

class FactStoreCountDueTest {
    private val nowMs: Long = 1_000_000L

    @Test
    fun countDueStatsCountsOnlyAttemptedFactsDueByNow() {
        val stats: List<FactStats> = listOf(
            fact(FactKey(FactKey.Op.MUL, 2, 3), attempts = 1, dueAtMs = nowMs - 1),
            fact(FactKey(FactKey.Op.MUL, 3, 4), attempts = 2, dueAtMs = nowMs),
            fact(FactKey(FactKey.Op.DIV, 8, 2), attempts = 1, dueAtMs = nowMs + 1),
            fact(FactKey(FactKey.Op.DIV, 9, 3), attempts = 0, dueAtMs = nowMs - 1),
            fact(FactKey(FactKey.Op.ADD, 1, 2), attempts = 3, dueAtMs = nowMs - 100),
        )
        assertEquals(2, countDueStats(stats.filter { it.key.op in setOf(FactKey.Op.MUL, FactKey.Op.DIV) }, nowMs))
        assertEquals(1, countDueStats(stats.filter { it.key.op == FactKey.Op.ADD }, nowMs))
        assertEquals(0, countDueStats(stats.filter { it.key.op == FactKey.Op.SUB }, nowMs))
    }

    private fun fact(key: FactKey, attempts: Int, dueAtMs: Long): FactStats {
        return FactStats(key = key, attempts = attempts, dueAtMs = dueAtMs)
    }
}

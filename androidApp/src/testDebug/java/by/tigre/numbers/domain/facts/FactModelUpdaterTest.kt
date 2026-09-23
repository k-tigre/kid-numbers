package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import org.junit.Assert.assertEquals
import org.junit.Test

class FactModelUpdaterTest {

    private val key: FactKey = FactKey(FactKey.Op.ADD, 2, 3)
    private val nowMs: Long = 1_000_000L
    private val dayMs: Long = 24L * 60 * 60 * 1000

    @Test
    fun adaptiveAlphaUsesTierBeforeFiveAttempts() {
        assertEquals(0.30f, FactModelUpdater.adaptiveAlpha(0), 0.0001f)
        assertEquals(0.30f, FactModelUpdater.adaptiveAlpha(4), 0.0001f)
    }

    @Test
    fun adaptiveAlphaUsesTierBeforeTwentyAttempts() {
        assertEquals(0.15f, FactModelUpdater.adaptiveAlpha(5), 0.0001f)
        assertEquals(0.15f, FactModelUpdater.adaptiveAlpha(19), 0.0001f)
    }

    @Test
    fun adaptiveAlphaUsesLowTierAfterTwentyAttempts() {
        assertEquals(0.08f, FactModelUpdater.adaptiveAlpha(20), 0.0001f)
        assertEquals(0.08f, FactModelUpdater.adaptiveAlpha(100), 0.0001f)
    }

    @Test
    fun correctAnswerUpdatesProbabilityWithEma() {
        val stats: FactStats = FactStats(key = key, p = 0.5f, attempts = 0)
        val updated: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = null, nowMs = nowMs)
        assertEquals(0.65f, updated.p, 0.0001f)
    }

    @Test
    fun wrongAnswerResetsStreak() {
        val stats: FactStats = FactStats(key = key, streak = 5, attempts = 10)
        val updated: FactStats = FactModelUpdater.next(stats, correct = false, timeMs = null, nowMs = nowMs)
        assertEquals(0, updated.streak)
    }

    @Test
    fun correctAnswerIncrementsStreak() {
        val stats: FactStats = FactStats(key = key, streak = 2, attempts = 3)
        val updated: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = null, nowMs = nowMs)
        assertEquals(3, updated.streak)
    }

    @Test
    fun correctAnswerSetsAvgTimeWhenZero() {
        val stats: FactStats = FactStats(key = key, avgTimeMs = 0)
        val updated: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = 1000L, nowMs = nowMs)
        assertEquals(1000, updated.avgTimeMs)
    }

    @Test
    fun correctAnswerUpdatesAvgTimeWithEma() {
        val stats: FactStats = FactStats(key = key, avgTimeMs = 1000)
        val updated: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = 2000L, nowMs = nowMs)
        assertEquals(1200, updated.avgTimeMs)
    }

    @Test
    fun nullTimeMsSkipsAvgTimeUpdate() {
        val stats: FactStats = FactStats(key = key, avgTimeMs = 1000)
        val updated: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = null, nowMs = nowMs)
        assertEquals(1000, updated.avgTimeMs)
    }

    @Test
    fun wrongAnswerSetsDueIntervalToOneDay() {
        val stats: FactStats = FactStats(key = key, intervalDays = 30)
        val updated: FactStats = FactModelUpdater.next(stats, correct = false, timeMs = null, nowMs = nowMs)
        assertEquals(1, updated.intervalDays)
        assertEquals(nowMs + dayMs, updated.dueAtMs)
    }

    @Test
    fun correctAnswerProgressesDueIntervals() {
        val fromZero: FactStats = FactModelUpdater.next(
            FactStats(key = key, intervalDays = 0),
            correct = true,
            timeMs = null,
            nowMs = nowMs,
        )
        assertEquals(1, fromZero.intervalDays)
        assertEquals(nowMs + dayMs, fromZero.dueAtMs)
        val fromOne: FactStats = FactModelUpdater.next(
            FactStats(key = key, intervalDays = 1),
            correct = true,
            timeMs = null,
            nowMs = nowMs,
        )
        assertEquals(3, fromOne.intervalDays)
        assertEquals(nowMs + 3 * dayMs, fromOne.dueAtMs)
        val fromThree: FactStats = FactModelUpdater.next(
            FactStats(key = key, intervalDays = 3),
            correct = true,
            timeMs = null,
            nowMs = nowMs,
        )
        assertEquals(6, fromThree.intervalDays)
        assertEquals(nowMs + 6 * dayMs, fromThree.dueAtMs)
        val fromThirty: FactStats = FactModelUpdater.next(
            FactStats(key = key, intervalDays = 30),
            correct = true,
            timeMs = null,
            nowMs = nowMs,
        )
        assertEquals(60, fromThirty.intervalDays)
        assertEquals(nowMs + 60 * dayMs, fromThirty.dueAtMs)
    }

    @Test
    fun wrongAnswerStoresLastErrorType() {
        val stats: FactStats = FactStats(key = key, attempts = 1)
        val updated: FactStats = FactModelUpdater.next(stats, correct = false, timeMs = null, nowMs = nowMs, errorType = "OFF_BY_ONE")
        assertEquals("OFF_BY_ONE", updated.lastErrorType)
    }

    @Test
    fun correctAnswerClearsLastErrorType() {
        val stats: FactStats = FactStats(key = key, attempts = 1, lastErrorType = "OFF_BY_ONE")
        val updated: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = null, nowMs = nowMs)
        assertEquals(null, updated.lastErrorType)
    }

    @Test
    fun nextIncrementsAttemptsAndCorrect() {
        val stats: FactStats = FactStats(key = key, attempts = 4, correct = 3)
        val correctUpdate: FactStats = FactModelUpdater.next(stats, correct = true, timeMs = null, nowMs = nowMs)
        assertEquals(5, correctUpdate.attempts)
        assertEquals(4, correctUpdate.correct)
        assertEquals(nowMs, correctUpdate.lastSeenMs)
        val wrongUpdate: FactStats = FactModelUpdater.next(stats, correct = false, timeMs = null, nowMs = nowMs)
        assertEquals(5, wrongUpdate.attempts)
        assertEquals(3, wrongUpdate.correct)
    }
}

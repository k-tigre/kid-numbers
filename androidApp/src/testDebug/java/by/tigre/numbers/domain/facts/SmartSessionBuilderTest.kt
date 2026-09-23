package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartSessionBuilderTest {

    private val builder: SmartSessionBuilder = SmartSessionBuilder()
    private val nowMs: Long = 1_000_000L

    @Test
    fun emptyUniverseReturnsEmptyList() {
        val result: List<FactKey> = builder.build(emptyList(), emptySet(), count = 10, nowMs = nowMs)
        assertTrue(result.isEmpty())
    }

    @Test
    fun resultSizeIsMinOfCountAndUniverseSize() {
        val universe: Set<FactKey> = setOf(
            FactKey(FactKey.Op.ADD, 1, 2),
            FactKey(FactKey.Op.ADD, 2, 3),
            FactKey(FactKey.Op.ADD, 3, 4),
        )
        val result: List<FactKey> = builder.build(emptyList(), universe, count = 10, nowMs = nowMs)
        assertEquals(3, result.size)
    }

    @Test
    fun neverReturnsKeysOutsideUniverse() {
        val universe: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0)).take(20).toSet()
        val stats: List<FactStats> = universe.map { key ->
            FactStats(key = key, p = 0.3f, attempts = 5, dueAtMs = nowMs - 1)
        }
        val result: List<FactKey> = builder.build(stats, universe, count = 15, nowMs = nowMs)
        assertTrue(result.all { it in universe })
    }

    @Test
    fun prefersUniqueKeysInSession() {
        val universe: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.ADD, 0))
        val result: List<FactKey> = builder.build(emptyList(), universe, count = 15, nowMs = nowMs)
        assertEquals(result.size, result.toSet().size)
    }

    @Test
    fun noviceSessionMostlyNewKeys() {
        val universe: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0)).take(30).toSet()
        val experiencedStats: List<FactStats> = universe.take(5).map { key ->
            FactStats(key = key, p = 0.3f, attempts = 5, dueAtMs = nowMs - 1)
        }
        val result: List<FactKey> = builder.build(experiencedStats, universe, count = 20, nowMs = nowMs)
        val experiencedKeys: Set<FactKey> = experiencedStats.map { it.key }.toSet()
        val newCount: Int = result.count { key -> key !in experiencedKeys }
        assertTrue(newCount >= 12)
    }

    @Test
    fun experiencedSessionPrefersDueAndWeak() {
        val universe: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0)).take(25).toSet()
        val dueWeak: List<FactKey> = universe.take(15).toList()
        val reviewNew: List<FactKey> = universe.drop(15).toList()
        val stats: List<FactStats> = buildList {
            dueWeak.forEach { key ->
                add(FactStats(key = key, p = 0.4f, attempts = 10, dueAtMs = nowMs - 1))
            }
            reviewNew.forEach { key ->
                add(FactStats(key = key, p = 0.7f, attempts = 10, dueAtMs = nowMs + 86_400_000))
            }
        }
        val result: List<FactKey> = builder.build(stats, universe, count = 20, nowMs = nowMs)
        val dueWeakCount: Int = result.count { it in dueWeak.toSet() }
        assertTrue(dueWeakCount >= 12)
    }

    @Test
    fun emptyDueBucketFillsFromWeakThenReviewThenNew() {
        val dueKey: FactKey = FactKey(FactKey.Op.MUL, 2, 3)
        val weakKey: FactKey = FactKey(FactKey.Op.MUL, 2, 4)
        val reviewKey: FactKey = FactKey(FactKey.Op.MUL, 2, 5)
        val newKey: FactKey = FactKey(FactKey.Op.MUL, 3, 4)
        val universe: Set<FactKey> = setOf(dueKey, weakKey, reviewKey, newKey)
        val stats: List<FactStats> = listOf(
            FactStats(key = weakKey, p = 0.4f, attempts = 5, dueAtMs = nowMs + 86_400_000),
            FactStats(key = reviewKey, p = 0.7f, attempts = 5, dueAtMs = nowMs + 86_400_000),
        )
        val result: List<FactKey> = builder.build(stats, universe, count = 4, nowMs = nowMs)
        assertEquals(4, result.size)
        assertTrue(result.contains(weakKey))
        assertTrue(result.contains(reviewKey))
        assertTrue(result.contains(newKey))
    }

    @Test
    fun multiOpMixRoughlyCapsSingleOpShare() {
        val addKeys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.ADD, 0)).take(15).toSet()
        val mulKeys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0)).take(15).toSet()
        val universe: Set<FactKey> = addKeys + mulKeys
        val stats: List<FactStats> = universe.map { key ->
            FactStats(key = key, p = 0.4f, attempts = 10, dueAtMs = nowMs - 1)
        }
        val result: List<FactKey> = builder.build(stats, universe, count = 20, nowMs = nowMs)
        val maxShare: Int = (20 * 0.6f).toInt().coerceAtLeast(1)
        val addCount: Int = result.count { it.op == FactKey.Op.ADD }
        val mulCount: Int = result.count { it.op == FactKey.Op.MUL }
        assertTrue(addCount <= maxShare)
        assertTrue(mulCount <= maxShare)
        assertTrue(addCount >= 1)
        assertTrue(mulCount >= 1)
    }

    @Test
    fun mixesMultiplicationAndDivisionWhenBothSelected() {
        val mulKeys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0)).take(20).toSet()
        val divKeys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.DIV, 0)).take(20).toSet()
        val universe: Set<FactKey> = mulKeys + divKeys
        val stats: List<FactStats> = universe.map { key ->
            FactStats(key = key, p = 0.4f, attempts = 10, dueAtMs = nowMs - 1)
        }
        val result: List<FactKey> = builder.build(stats, universe, count = 15, nowMs = nowMs)
        val mulCount: Int = result.count { it.op == FactKey.Op.MUL }
        val divCount: Int = result.count { it.op == FactKey.Op.DIV }
        assertTrue(mulCount >= 4)
        assertTrue(divCount >= 4)
        var sameOpStreak: Int = 1
        var maxSameOpStreak: Int = 1
        for (index in 1 until result.size) {
            if (result[index].op == result[index - 1].op) {
                sameOpStreak++
                maxSameOpStreak = maxOf(maxSameOpStreak, sameOpStreak)
            } else {
                sameOpStreak = 1
            }
        }
        assertTrue(maxSameOpStreak <= 4)
    }

    @Test
    fun avoidsThreeConsecutiveSameAnchorWhenPossible() {
        val keys: List<FactKey> = listOf(
            FactKey(FactKey.Op.MUL, 2, 3),
            FactKey(FactKey.Op.MUL, 2, 4),
            FactKey(FactKey.Op.MUL, 2, 5),
            FactKey(FactKey.Op.MUL, 3, 4),
            FactKey(FactKey.Op.MUL, 3, 5),
            FactKey(FactKey.Op.MUL, 4, 5),
        )
        val universe: Set<FactKey> = keys.toSet()
        val stats: List<FactStats> = keys.map { key ->
            FactStats(key = key, p = 0.4f, attempts = 5, dueAtMs = nowMs - 1)
        }
        val result: List<FactKey> = builder.build(stats, universe, count = 6, nowMs = nowMs)
        for (index in 0..result.size - 3) {
            val window: List<FactKey> = result.subList(index, index + 3)
            val sameA: Boolean = window.map { anchor(it) }.distinct().size == 1
            val sameB: Boolean = window.map { it.b }.distinct().size == 1
            assertTrue(!sameA || !sameB)
        }
    }

    private fun anchor(key: FactKey): Int {
        return when (key.op) {
            FactKey.Op.MUL, FactKey.Op.DIV -> minOf(key.a, key.b)
            FactKey.Op.ADD, FactKey.Op.SUB -> key.a
        }
    }
}

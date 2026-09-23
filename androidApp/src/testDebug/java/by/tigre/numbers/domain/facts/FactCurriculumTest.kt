package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil

class FactCurriculumTest {

    private fun masteryThreshold(bandSize: Int): Int = ceil(bandSize * 0.70f).toInt()

    @Test
    fun bandKeySetsAreNonEmptyForAllOps() {
        FactKey.Op.entries.forEach { op ->
            assertTrue(FactCurriculum.keysInBand(BandId(op, 0)).isNotEmpty())
            assertTrue(FactCurriculum.keysInBand(BandId(op, 1)).isNotEmpty())
            assertTrue(FactCurriculum.allKeys(op).isNotEmpty())
        }
    }

    @Test
    fun bandsDoNotOverlapWithinSameOp() {
        FactKey.Op.entries.forEach { op ->
            val band0: Set<FactKey> = FactCurriculum.keysInBand(BandId(op, 0))
            val band1: Set<FactKey> = FactCurriculum.keysInBand(BandId(op, 1))
            val band2: Set<FactKey> = FactCurriculum.keysInBand(BandId(op, 2))
            assertTrue(band0.intersect(band1).isEmpty())
            assertTrue(band0.intersect(band2).isEmpty())
            assertTrue(band1.intersect(band2).isEmpty())
        }
    }

    @Test
    fun allKeysIsUnionOfAllBands() {
        FactKey.Op.entries.forEach { op ->
            val union: Set<FactKey> = (0..2).flatMap { index ->
                FactCurriculum.keysInBand(BandId(op, index)).toList()
            }.toSet()
            assertEquals(union, FactCurriculum.allKeys(op))
        }
    }

    @Test
    fun bandZeroAlwaysOpenWithEmptyStats() {
        FactKey.Op.entries.forEach { op ->
            val open: Set<BandId> = FactCurriculum.openBands(op, emptyList())
            assertTrue(BandId(op, 0) in open)
        }
    }

    @Test
    fun bandOneClosedUntilBandZeroMastered() {
        val band0Keys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.ADD, 0))
        val emptyOpen: Set<BandId> = FactCurriculum.openBands(FactKey.Op.ADD, emptyList())
        assertFalse(BandId(FactKey.Op.ADD, 1) in emptyOpen)
        val partialStats: List<FactStats> = band0Keys.take(band0Keys.size / 2).map { key ->
            FactStats(key = key, p = 0.9f, attempts = 5)
        }
        assertFalse(BandId(FactKey.Op.ADD, 1) in FactCurriculum.openBands(FactKey.Op.ADD, partialStats))
    }

    @Test
    fun bandOneOpensWhenBandZeroMasteredEnough() {
        val band0Keys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0))
        val threshold: Int = masteryThreshold(band0Keys.size)
        val masteredStats: List<FactStats> = band0Keys.take(threshold).map { key ->
            FactStats(key = key, p = 0.8f, attempts = 1)
        }
        val open: Set<BandId> = FactCurriculum.openBands(FactKey.Op.MUL, masteredStats)
        assertTrue(BandId(FactKey.Op.MUL, 0) in open)
        assertTrue(BandId(FactKey.Op.MUL, 1) in open)
    }

    @Test
    fun bandTwoNeverOpensEvenWhenFullyMastered() {
        FactKey.Op.entries.forEach { op ->
            val allStats: List<FactStats> = FactCurriculum.allKeys(op).map { key ->
                FactStats(key = key, p = 1.0f, attempts = 10)
            }
            val open: Set<BandId> = FactCurriculum.openBands(op, allStats)
            assertFalse(BandId(op, 2) in open)
        }
    }

    @Test
    fun addBandTwoExistsInAllKeysButNotSelectableByDefault() {
        val band2: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.ADD, 2))
        assertTrue(band2.isNotEmpty())
        assertTrue(band2.all { it in FactCurriculum.allKeys(FactKey.Op.ADD) })
        val open: Set<BandId> = FactCurriculum.openBands(
            FactKey.Op.ADD,
            FactCurriculum.allKeys(FactKey.Op.ADD).map { FactStats(key = it, p = 1.0f, attempts = 10) },
        )
        assertFalse(BandId(FactKey.Op.ADD, 2) in open)
    }

    @Test
    fun selectableUniverseRespectsEnabledOps() {
        val stats: List<FactStats> = emptyList()
        val mulOnly: Set<FactKey> = FactCurriculum.selectableUniverse(setOf(FactKey.Op.MUL), stats)
        assertTrue(mulOnly.all { it.op == FactKey.Op.MUL })
        assertTrue(mulOnly.isNotEmpty())
        val addMul: Set<FactKey> = FactCurriculum.selectableUniverse(
            setOf(FactKey.Op.ADD, FactKey.Op.MUL),
            stats,
        )
        assertTrue(addMul.all { it.op == FactKey.Op.ADD || it.op == FactKey.Op.MUL })
        assertTrue(addMul.any { it.op == FactKey.Op.ADD })
        assertTrue(addMul.any { it.op == FactKey.Op.MUL })
    }

    @Test
    fun isBandMasteredEnoughRequiresSeventyPercentKnown() {
        val bandKeys: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.SUB, 0))
        val threshold: Int = masteryThreshold(bandKeys.size)
        val belowThreshold: List<FactStats> = bandKeys.take(threshold - 1).map { key ->
            FactStats(key = key, p = 0.9f, attempts = 1)
        }
        assertFalse(FactCurriculum.isBandMasteredEnough(bandKeys, belowThreshold))
        val atThreshold: List<FactStats> = bandKeys.take(threshold).map { key ->
            FactStats(key = key, p = 0.9f, attempts = 1)
        }
        assertTrue(FactCurriculum.isBandMasteredEnough(bandKeys, atThreshold))
    }

    @Test
    fun masteryCountsAttemptsWithoutHighProbability() {
        val key: FactKey = FactCurriculum.keysInBand(BandId(FactKey.Op.DIV, 0)).first()
        val bandKeys: Set<FactKey> = setOf(key)
        val stats: List<FactStats> = listOf(FactStats(key = key, p = 0.2f, attempts = 3))
        assertTrue(FactCurriculum.isBandMasteredEnough(bandKeys, stats))
    }

    @Test
    fun mulBandZeroUsesMultipliersTwoThroughFive() {
        val band0: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.MUL, 0))
        assertTrue(FactKey(FactKey.Op.MUL, 2, 5) in band0)
        assertTrue(FactKey(FactKey.Op.MUL, 3, 7) in band0)
        assertFalse(FactKey(FactKey.Op.MUL, 7, 8) in band0)
    }

    @Test
    fun addBandZeroHasNoCarryBeyondTen() {
        val band0: Set<FactKey> = FactCurriculum.keysInBand(BandId(FactKey.Op.ADD, 0))
        assertTrue(FactKey(FactKey.Op.ADD, 3, 7) in band0)
        assertFalse(FactKey(FactKey.Op.ADD, 6, 7) in band0)
    }
}

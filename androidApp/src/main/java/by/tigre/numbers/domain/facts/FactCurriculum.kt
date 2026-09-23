package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats

data class BandId(val op: FactKey.Op, val index: Int)

object FactCurriculum {

    const val maxOpenBandIndex: Int = 1

    fun keysInBand(band: BandId): Set<FactKey> {
        return when (band.op) {
            FactKey.Op.MUL -> mulBand(band.index)
            FactKey.Op.DIV -> divBand(band.index)
            FactKey.Op.ADD -> addBand(band.index)
            FactKey.Op.SUB -> subBand(band.index)
        }
    }

    fun allKeys(op: FactKey.Op): Set<FactKey> {
        return (0..2).flatMap { index -> keysInBand(BandId(op, index)).toList() }.toSet()
    }

    fun openBands(op: FactKey.Op, stats: List<FactStats>): Set<BandId> {
        val open: MutableSet<BandId> = mutableSetOf(BandId(op, 0))
        for (bandIndex in 0 until maxOpenBandIndex) {
            val bandKeys: Set<FactKey> = keysInBand(BandId(op, bandIndex))
            if (bandKeys.isEmpty()) break
            if (isBandMasteredEnough(bandKeys, stats)) {
                open.add(BandId(op, bandIndex + 1))
            } else {
                break
            }
        }
        return open
    }

    fun selectableUniverse(enabledOps: Set<FactKey.Op>, stats: List<FactStats>): Set<FactKey> {
        return enabledOps.flatMap { op ->
            openBands(op, stats).flatMap { band -> keysInBand(band).toList() }
        }.toSet()
    }

    fun isBandMasteredEnough(bandKeys: Set<FactKey>, stats: List<FactStats>): Boolean {
        if (bandKeys.isEmpty()) return false
        val statsByKey: Map<FactKey, FactStats> = stats.associateBy { it.key }
        val known: Int = bandKeys.count { key ->
            val stat: FactStats? = statsByKey[key]
            stat != null && (stat.p >= 0.75f || stat.attempts >= 3)
        }
        return known >= (bandKeys.size * 0.70f)
    }

    private fun mulBand(index: Int): Set<FactKey> {
        val band0: Set<FactKey> = mulFromMultipliers(2..5)
        return when (index) {
            0 -> band0
            1 -> mulFromMultipliers(6..9) - band0
            2 -> fullMulTable() - band0 - mulFromMultipliers(6..9)
            else -> emptySet()
        }
    }

    private fun divBand(index: Int): Set<FactKey> {
        val band0: Set<FactKey> = divFromMultipliers(2..5)
        return when (index) {
            0 -> band0
            1 -> divFromMultipliers(6..9) - band0
            2 -> divFromMulKeys(mulBand(2)) - band0 - divFromMultipliers(6..9)
            else -> emptySet()
        }
    }

    private fun addBand(index: Int): Set<FactKey> {
        val band0: Set<FactKey> = buildSet {
            for (a in 0..10) {
                for (b in a..10) {
                    if (a + b <= 10) add(FactKey(FactKey.Op.ADD, a, b))
                }
            }
        }
        val band1: Set<FactKey> = buildSet {
            for (a in 0..10) {
                for (b in a..10) {
                    if (a + b in 11..20) add(FactKey(FactKey.Op.ADD, a, b))
                }
            }
        }
        return when (index) {
            0 -> band0
            1 -> band1
            2 -> buildSet {
                for (a in 0..50) {
                    for (b in a..50) {
                        if (a + b in 21..100) add(FactKey(FactKey.Op.ADD, a, b))
                    }
                }
            }
            else -> emptySet()
        }
    }

    private fun subBand(index: Int): Set<FactKey> {
        val band0: Set<FactKey> = buildSet {
            for (minuend in 0..10) {
                for (subtrahend in 0..minuend) {
                    add(FactKey(FactKey.Op.SUB, minuend, subtrahend))
                }
            }
        }
        val band1: Set<FactKey> = buildSet {
            for (minuend in 11..20) {
                for (subtrahend in 0..minuend) {
                    add(FactKey(FactKey.Op.SUB, minuend, subtrahend))
                }
            }
        }
        return when (index) {
            0 -> band0
            1 -> band1
            2 -> buildSet {
                for (minuend in 21..100) {
                    for (subtrahend in 0..minuend) {
                        add(FactKey(FactKey.Op.SUB, minuend, subtrahend))
                    }
                }
            }
            else -> emptySet()
        }
    }

    private fun mulFromMultipliers(multipliers: IntRange): Set<FactKey> {
        return buildSet {
            for (m in multipliers) {
                for (n in 1..10) {
                    val first: Int = minOf(m, n)
                    val second: Int = maxOf(m, n)
                    add(FactKey(FactKey.Op.MUL, first, second))
                }
            }
        }
    }

    private fun fullMulTable(): Set<FactKey> {
        return buildSet {
            for (a in 1..10) {
                for (b in a..10) {
                    add(FactKey(FactKey.Op.MUL, a, b))
                }
            }
        }
    }

    private fun divFromMultipliers(multipliers: IntRange): Set<FactKey> {
        return buildSet {
            for (m in multipliers) {
                for (n in 1..10) {
                    add(FactKey(FactKey.Op.DIV, m * n, m))
                }
            }
        }
    }

    private fun divFromMulKeys(mulKeys: Set<FactKey>): Set<FactKey> {
        return buildSet {
            mulKeys.forEach { key ->
                val product: Int = key.a * key.b
                add(FactKey(FactKey.Op.DIV, product, key.a))
                add(FactKey(FactKey.Op.DIV, product, key.b))
            }
        }
    }
}

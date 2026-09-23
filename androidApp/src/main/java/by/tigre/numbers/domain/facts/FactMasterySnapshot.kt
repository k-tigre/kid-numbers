package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats

data class FactMasterySnapshot(
    val opProgress: List<OpProgress>,
    val weakFactKeys: List<FactKey>,
    val dueTodayCount: Int,
) {
    data class OpProgress(val op: FactKey.Op, val mastered: Int, val total: Int)

    companion object {
        fun compute(allStats: List<FactStats>, nowMs: Long): FactMasterySnapshot {
            val statsByKey: Map<FactKey, FactStats> = allStats.associateBy { it.key }
            val opProgress: List<OpProgress> = FactKey.Op.entries.map { op ->
                val openKeys: Set<FactKey> = FactCurriculum.openBands(op, allStats)
                    .flatMap { band -> FactCurriculum.keysInBand(band) }
                    .toSet()
                val targetMs: Int = targetMsFor(op)
                val mastered: Int = openKeys.count { key ->
                    val stat: FactStats? = statsByKey[key]
                    stat != null && isMastered(stat, targetMs)
                }
                OpProgress(op = op, mastered = mastered, total = openKeys.size)
            }
            val weakFactKeys: List<FactKey> = allStats
                .filter { it.attempts > 0 }
                .sortedBy { it.p }
                .take(5)
                .map { it.key }
            val dueTodayCount: Int = allStats.count { it.dueAtMs > 0L && it.dueAtMs <= nowMs }
            return FactMasterySnapshot(
                opProgress = opProgress,
                weakFactKeys = weakFactKeys,
                dueTodayCount = dueTodayCount,
            )
        }

        private fun targetMsFor(op: FactKey.Op): Int = when (op) {
            FactKey.Op.ADD, FactKey.Op.SUB -> 2500
            FactKey.Op.MUL -> 3000
            FactKey.Op.DIV -> 4000
        }

        private fun isMastered(stat: FactStats, targetMs: Int): Boolean {
            return stat.p > 0.9f && stat.avgTimeMs in 1..targetMs
        }
    }
}

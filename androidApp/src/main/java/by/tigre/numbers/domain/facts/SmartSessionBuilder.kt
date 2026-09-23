package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import kotlin.random.Random

class SmartSessionBuilder(
    private val random: Random = Random.Default,
) {

    fun build(
        stats: List<FactStats>,
        universe: Set<FactKey>,
        count: Int,
        nowMs: Long,
    ): List<FactKey> {
        if (universe.isEmpty()) return emptyList()
        val targetSize: Int = minOf(count, universe.size)
        val statsByKey: Map<FactKey, FactStats> = stats.associateBy { it.key }
        val universeStats: List<FactStats> = universe.map { key ->
            statsByKey[key] ?: FactStats(key = key)
        }
        val totalAttempts: Int = universeStats.sumOf { it.attempts }
        val isNovice: Boolean = totalAttempts < 30
        val weights: Map<Bucket, Double> = if (isNovice) {
            mapOf(
                Bucket.DUE to 0.0,
                Bucket.WEAK to 0.2,
                Bucket.REVIEW to 0.1,
                Bucket.NEW to 0.7,
            )
        } else {
            mapOf(
                Bucket.DUE to 0.4,
                Bucket.WEAK to 0.3,
                Bucket.REVIEW to 0.2,
                Bucket.NEW to 0.1,
            )
        }
        val buckets: Map<Bucket, MutableList<FactKey>> = buildBuckets(universeStats, nowMs)
        val selected: List<FactKey> = selectKeys(
            buckets = buckets,
            weights = weights,
            targetSize = targetSize,
            universe = universe,
        )
        return orderSession(keys = selected)
    }

    private enum class Bucket {
        DUE,
        WEAK,
        REVIEW,
        NEW,
    }

    private fun buildBuckets(
        universeStats: List<FactStats>,
        nowMs: Long,
    ): Map<Bucket, MutableList<FactKey>> {
        val buckets: Map<Bucket, MutableList<FactKey>> = Bucket.entries.associateWith { mutableListOf() }
        universeStats.forEach { stat ->
            val key: FactKey = stat.key
            if (stat.attempts == 0) {
                buckets.getValue(Bucket.NEW).add(key)
            }
            if (stat.dueAtMs <= nowMs && stat.attempts > 0) {
                buckets.getValue(Bucket.DUE).add(key)
            }
            if (stat.attempts > 0 && stat.p < 0.6f) {
                buckets.getValue(Bucket.WEAK).add(key)
            }
            if (stat.p >= 0.6f && stat.p <= 0.85f) {
                buckets.getValue(Bucket.REVIEW).add(key)
            }
        }
        buckets.values.forEach { list -> list.shuffle(random) }
        return buckets
    }

    private fun selectKeys(
        buckets: Map<Bucket, MutableList<FactKey>>,
        weights: Map<Bucket, Double>,
        targetSize: Int,
        universe: Set<FactKey>,
    ): List<FactKey> {
        val bucketLists: Map<Bucket, MutableList<FactKey>> = buckets.mapValues { (_, keys) ->
            keys.distinct().toMutableList()
        }
        val slots: Map<Bucket, Int> = allocateSlots(weights, targetSize)
        val picked: MutableSet<FactKey> = linkedSetOf()
        val bucketOrder: List<Bucket> = listOf(Bucket.DUE, Bucket.WEAK, Bucket.REVIEW, Bucket.NEW)
        val opsInUniverse: Set<FactKey.Op> = universe.map { it.op }.toSet()
        // Soft balance: allow up to ~60% per op so sessions can fill to targetSize with 2 ops
        val maxPerOp: Int = if (opsInUniverse.size >= 2) {
            (targetSize * 0.6f).toInt().coerceAtLeast((targetSize + opsInUniverse.size - 1) / opsInUniverse.size)
        } else {
            targetSize
        }
        val opCounts: MutableMap<FactKey.Op, Int> = mutableMapOf()
        bucketOrder.forEach { bucket ->
            var remainingSlots: Int = slots.getValue(bucket)
            while (remainingSlots > 0 && picked.size < targetSize) {
                val key: FactKey? = pickFromBucketChain(
                    start = bucket,
                    bucketLists = bucketLists,
                    picked = picked,
                    opCounts = opCounts,
                    maxPerOp = maxPerOp,
                    universe = universe,
                )
                if (key == null) break
                picked.add(key)
                removeFromAllBuckets(key, bucketLists)
                opCounts[key.op] = (opCounts[key.op] ?: 0) + 1
                remainingSlots--
            }
        }
        while (picked.size < targetSize) {
            val key: FactKey? = pickFromBucketChain(
                start = Bucket.DUE,
                bucketLists = bucketLists,
                picked = picked,
                opCounts = opCounts,
                maxPerOp = maxPerOp,
                universe = universe,
            ) ?: pickRandomRemaining(universe, picked, opCounts, targetSize)
            if (key == null) break
            picked.add(key)
            removeFromAllBuckets(key, bucketLists)
            opCounts[key.op] = (opCounts[key.op] ?: 0) + 1
        }
        return picked.toList()
    }

    private fun allocateSlots(weights: Map<Bucket, Double>, targetSize: Int): Map<Bucket, Int> {
        val raw: List<Pair<Bucket, Double>> = Bucket.entries.map { bucket ->
            bucket to weights.getValue(bucket) * targetSize
        }
        val floors: Map<Bucket, Int> = raw.associate { (bucket, value) -> bucket to value.toInt() }
        var assigned: Int = floors.values.sum()
        val remainders: List<Pair<Bucket, Double>> = raw.map { (bucket, value) ->
            bucket to (value - value.toInt())
        }.sortedByDescending { it.second }
        val slots: MutableMap<Bucket, Int> = floors.toMutableMap()
        var index: Int = 0
        while (assigned < targetSize && index < remainders.size) {
            val bucket: Bucket = remainders[index].first
            slots[bucket] = slots.getValue(bucket) + 1
            assigned++
            index++
        }
        return slots
    }

    private fun pickFromBucketChain(
        start: Bucket,
        bucketLists: Map<Bucket, MutableList<FactKey>>,
        picked: Set<FactKey>,
        opCounts: Map<FactKey.Op, Int>,
        maxPerOp: Int,
        universe: Set<FactKey>,
    ): FactKey? {
        val chain: List<Bucket> = listOf(Bucket.DUE, Bucket.WEAK, Bucket.REVIEW, Bucket.NEW)
            .dropWhile { it != start }
        chain.forEach { bucket ->
            val candidates: List<FactKey> = bucketLists.getValue(bucket)
                .filter { key -> key !in picked && (opCounts[key.op] ?: 0) < maxPerOp }
            if (candidates.isNotEmpty()) {
                val minOpCount: Int = candidates.minOf { opCounts[it.op] ?: 0 }
                val balanced: List<FactKey> = candidates.filter { (opCounts[it.op] ?: 0) == minOpCount }
                return balanced[random.nextInt(balanced.size)]
            }
        }
        return pickRandomRemaining(universe, picked, opCounts, maxPerOp)
    }

    private fun pickRandomRemaining(
        universe: Set<FactKey>,
        picked: Set<FactKey>,
        opCounts: Map<FactKey.Op, Int>,
        maxPerOp: Int,
    ): FactKey? {
        val remaining: List<FactKey> = universe
            .filter { key -> key !in picked && (opCounts[key.op] ?: 0) < maxPerOp }
            .toList()
        if (remaining.isEmpty()) {
            val anyLeft: List<FactKey> = universe.filter { it !in picked }.toList()
            if (anyLeft.isEmpty()) return null
            return anyLeft[random.nextInt(anyLeft.size)]
        }
        val minOpCount: Int = remaining.minOf { opCounts[it.op] ?: 0 }
        val balanced: List<FactKey> = remaining.filter { (opCounts[it.op] ?: 0) == minOpCount }
        return balanced[random.nextInt(balanced.size)]
    }

    private fun removeFromAllBuckets(
        key: FactKey,
        bucketLists: Map<Bucket, MutableList<FactKey>>,
    ) {
        bucketLists.values.forEach { list -> list.remove(key) }
    }

    private fun orderSession(keys: List<FactKey>): List<FactKey> {
        if (keys.size <= 1) return keys
        val remaining: MutableList<FactKey> = keys.toMutableList()
        val result: MutableList<FactKey> = mutableListOf()
        while (remaining.isNotEmpty()) {
            val bestPenalty: Int = remaining.minOf { interleavePenalty(it, result) }
            val candidates: List<FactKey> = remaining.filter { interleavePenalty(it, result) == bestPenalty }
            val next: FactKey = candidates[random.nextInt(candidates.size)]
            result.add(next)
            remaining.remove(next)
        }
        return result
    }

    private fun interleavePenalty(key: FactKey, result: List<FactKey>): Int {
        if (result.isEmpty()) return 0
        val last: FactKey = result.last()
        var penalty: Int = 0
        if (last.op == key.op) {
            penalty += 6
            if (result.size >= 2 && result[result.lastIndex - 1].op == key.op) {
                penalty += 8
            }
        }
        if (areRelatedFacts(last, key)) {
            penalty += 10
        }
        val anchor: Int = anchorValue(key)
        if (anchorValue(last) == anchor) {
            penalty += 3
        }
        if (last.b == key.b && key.op == last.op) {
            penalty += 2
        }
        if (result.size >= 2) {
            val lastTwo: List<FactKey> = result.takeLast(2)
            if (lastTwo.all { anchorValue(it) == anchor }) {
                penalty += 4
            }
        }
        return penalty
    }

    private fun areRelatedFacts(a: FactKey, b: FactKey): Boolean {
        if (a == b) return true
        val pair: Set<FactKey.Op> = setOf(a.op, b.op)
        if (pair != setOf(FactKey.Op.MUL, FactKey.Op.DIV)) return false
        val mul: FactKey = if (a.op == FactKey.Op.MUL) a else b
        val div: FactKey = if (a.op == FactKey.Op.DIV) a else b
        val product: Int = mul.a * mul.b
        return div.a == product && (div.b == mul.a || div.b == mul.b)
    }

    private fun anchorValue(key: FactKey): Int {
        return when (key.op) {
            FactKey.Op.MUL, FactKey.Op.DIV -> minOf(key.a, key.b)
            FactKey.Op.ADD, FactKey.Op.SUB -> key.a
        }
    }
}

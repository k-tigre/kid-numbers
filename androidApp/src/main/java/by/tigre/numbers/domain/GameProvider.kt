package by.tigre.numbers.domain

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import by.tigre.numbers.entity.GameType
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

interface GameProvider {
    fun provide(settings: GameSettings): GameOptions

    class Impl(
        private val analytics: EventAnalytics,
        private val durationProvider: GameDurationProvider
    ) : GameProvider {
        override fun provide(settings: GameSettings): GameOptions {
            val startTime = System.currentTimeMillis()
            return when (settings) {
                is GameSettings.Additional -> generateQuestions(settings)
                is GameSettings.Multiplication -> generateQuestions(settings)
                is Equations -> generateQuestions(settings)
            }.also {
                analytics.trackEvent(
                    Event.Action.Logic.GenerateQuestions(
                        duration = System.currentTimeMillis() - startTime,
                        difficult = settings.difficult,
                        type = it.type
                    )
                )
            }
        }

        private fun generateQuestions(settings: GameSettings.Multiplication): GameOptions {
            val allQuestions = settings.selectedNumbers
                .flatMap { first ->
                    val questions = (1..10).map { second ->
                        multiplicationOperation(settings, first, second)
                    }
                    when (settings.difficult) {
                        Difficult.Easy -> questions.shuffled()
                        Difficult.Medium -> (questions + questions).shuffled().take(15)
                        Difficult.Hard -> (questions + questions).shuffled()
                    }
                }
                .shuffledWithoutConsecutive()
            return GameOptions(
                questions = allQuestions,
                duration = durationProvider.provide(settings),
                difficult = settings.difficult,
                type = if (settings.isPositive) GameType.Multiplication else GameType.Division
            )
        }

        private fun generateQuestions(settings: GameSettings.Additional): GameOptions {
            val count: Int = when (settings.difficult) {
                Difficult.Easy -> 10
                Difficult.Medium -> 15
                Difficult.Hard -> 20
            }

            val range = settings.range
            val allQuestions = buildQuestionsPreferUnique(count) {
                val x = randomNonSame(range.min + range.min, range.max, 0, 1)
                val a = randomNonSame(range.min, x - range.min, 0, 2)
                val b = x - a
                if (settings.isPositive) {
                    Operation.Additional(a = a, b = b)
                } else {
                    Operation.Subtraction(x = a, b = b)
                }
            }.shuffledWithoutConsecutive(Random)

            return GameOptions(
                questions = allQuestions,
                duration = durationProvider.provide(settings),
                difficult = settings.difficult,
                type = if (settings.isPositive) GameType.Additional else GameType.Subtraction
            )
        }

        private fun generateQuestions(settings: Equations): GameOptions {
            val count: Int = when (settings.difficult) {
                Difficult.Easy -> 10
                Difficult.Medium -> 15
                Difficult.Hard -> 20
            }

            val questions = when (settings.dimension) {
                Equations.Dimension.Single -> {

                    when (settings.type) {
                        Equations.Type.Additional -> generateSingleEquationsQuestionsForAdditional(settings.range, count)
                        Equations.Type.Multiplication -> generateSingleEquationsQuestionsForMultiplication(settings.range, count)
                        Equations.Type.Both -> generateSingleEquationsQuestionsBoth(settings.range, count)
                    }
                }

                Equations.Dimension.Double -> generateDoubleEquationsQuestions(
                    ranges = settings.range,
                    count = count,
                    type = settings.type
                )
            }

            return GameOptions(
                questions = questions,
                duration = durationProvider.provide(settings),
                difficult = settings.difficult,
                type = GameType.Equations
            )
        }

        private fun generateSingleEquationsQuestionsBoth(
            range: GameSettings.Range,
            count: Int,
        ): List<GameOptions.Question.Equation.Single> {
            // a + b * X = c
            val min: Int
            val max: Int

            if (range.withNegative) {
                min = -range.max
                max = range.max
            } else {
                min = 0
                max = range.max
            }
            return buildQuestionsPreferUnique(count) {
                val x: Int
                val c: Int
                val b: Int = randomNonSame(min = min / 2, max = max / 2, target = 0, fraction = 10)
                val a: Int = randomNonSame(min = min, max = max, target = 0, fraction = if (range.withNegative) 2 else 10)
                fun x(deep: Int = 0): Int {
                    val cTmp = randomNonSame(min = min, max = max, target = a, fraction = 1)
                    val xTmp = ((cTmp - a).toFloat() / b).coerceIn(range.min.toFloat(), range.max.toFloat()).roundToInt()
                    return if ((xTmp == 1 || xTmp == 0) && deep < 5) x(deep + 1) else xTmp
                }
                x = x()
                c = a + b * x
                GameOptions.Question.Equation.Single(
                    x = x,
                    title = "$a ${if (b > 0) "+" else "-"} ${abs(b)} * X = $c\nX = %s"
                )
            }.shuffledWithoutConsecutive()
        }

        private fun generateSingleEquationsQuestionsForAdditional(
            range: GameSettings.Range,
            count: Int,
        ): List<GameOptions.Question.Equation.Single> {
            // a + X = c
            val min: Int = range.min
            val max: Int = range.max

            return buildQuestionsPreferUnique(count) {
                val x: Int
                val a: Int = randomNonSame(min = min, max = if (range.withNegative) max else max / 2, target = 0, fraction = 3)
                val c = randomNonSame(min = if (range.withNegative) min else a, max = (max - a).coerceAtMost(max), target = a, fraction = 1)
                x = c - a
                GameOptions.Question.Equation.Single(
                    x = x,
                    title = "$a + X = $c\nX = %s"
                )
            }.shuffledWithoutConsecutive()
        }

        private fun generateSingleEquationsQuestionsForMultiplication(
            range: GameSettings.Range,
            count: Int,
        ): List<GameOptions.Question.Equation.Single> {
            // b * X = c
            val min: Int = range.min
            val max: Int = range.max

            return buildQuestionsPreferUnique(count) {
                val b = randomNonSame(min = min / 2, max = max / 2, target = 0, fraction = 3)
                val cTmp = randomNonSame(min = min, max = max, target = min, fraction = 1)
                val x = (cTmp.toFloat() / b).coerceIn(min.toFloat(), range.max.toFloat()).roundToInt()
                val c = b * x
                GameOptions.Question.Equation.Single(
                    x = x,
                    title = "$b * X = $c\nX = %s"
                )
            }.shuffledWithoutConsecutive()
        }

        private fun generateDoubleEquationsQuestions(
            ranges: GameSettings.Range,
            count: Int,
            type: Equations.Type
        ): List<GameOptions.Question.Equation.Double> {
            // a1 + b1 * X + c1 * Y = d1
            // a2 + b2 * X + c2 * Y = d2
            val min = ranges.min
            val max = ranges.max

            return buildQuestionsPreferUnique(count) {
                val system = generateDoubleEquationSystem(type, min, max, ranges.withNegative)
                GameOptions.Question.Equation.Double(
                    x = system.x,
                    y = system.y,
                    title = "${formatDoubleEquation(system.eq1.a, system.eq1.b, system.eq1.c, system.d1)}\n" +
                            formatDoubleEquation(system.eq2.a, system.eq2.b, system.eq2.c, system.d2)
                )
            }.shuffledWithoutConsecutive()
        }

        private fun multiplicationOperation(
            settings: GameSettings.Multiplication,
            first: Int,
            second: Int,
        ): Operation = if (settings.isPositive) {
            if (settings.difficult == Difficult.Hard && Random.nextBoolean()) {
                Operation.Multiplication(first = second, second = first)
            } else {
                Operation.Multiplication(first = first, second = second)
            }
        } else {
            Operation.Division(x = second, second = first)
        }

        private inline fun <Q : GameOptions.Question> buildUniqueQuestions(
            count: Int,
            maxAttempts: Int = count * 100,
            crossinline generate: () -> Q,
        ): List<Q> {
            val result = ArrayList<Q>(count)
            val seenTitles = HashSet<String>(count)
            var attempts = 0
            while (result.size < count && attempts < maxAttempts) {
                attempts++
                val question = try {
                    generate()
                } catch (_: IllegalArgumentException) {
                    continue
                }
                if (seenTitles.add(question.title)) {
                    result.add(question)
                }
            }
            return result
        }

        private inline fun <Q : GameOptions.Question> buildQuestionsPreferUnique(
            count: Int,
            maxAttempts: Int = count * 100,
            crossinline generate: () -> Q,
        ): List<Q> {
            val uniqueQuestions = buildUniqueQuestions(count, maxAttempts, generate)
            if (uniqueQuestions.size >= count) {
                return uniqueQuestions
            }
            val result = uniqueQuestions.toMutableList()
            while (result.size < count) {
                val question = try {
                    generate()
                } catch (_: IllegalArgumentException) {
                    continue
                }
                result.add(question)
            }
            return result
        }

        private fun <Q : GameOptions.Question> List<Q>.shuffledWithoutConsecutive(
            random: Random = Random,
        ): List<Q> {
            if (size <= 1) {
                return this
            }
            val shuffled = shuffled(random).toMutableList()
            if (!shuffled.hasConsecutiveDuplicates()) {
                return shuffled
            }
            return shuffled.reorderedWithoutConsecutive()
        }

        private fun <Q : GameOptions.Question> List<Q>.hasConsecutiveDuplicates(): Boolean =
            zipWithNext().any { (previous, current) -> previous.title == current.title }

        private fun <Q : GameOptions.Question> MutableList<Q>.reorderedWithoutConsecutive(): List<Q> {
            val remaining = groupBy { it.title }
                .mapValues { (_, items) -> ArrayDeque(items) }
                .toMutableMap()
            val result = ArrayList<Q>(size)
            var lastTitle: String? = null
            repeat(size) {
                val nextEntry = remaining.entries
                    .filter { (title, queue) -> queue.isNotEmpty() && title != lastTitle }
                    .maxByOrNull { (_, queue) -> queue.size }
                val entry = nextEntry ?: remaining.entries.first { (_, queue) -> queue.isNotEmpty() }
                val question = entry.value.removeFirst()
                if (entry.value.isEmpty()) {
                    remaining.remove(entry.key)
                }
                result.add(question)
                lastTitle = question.title
            }
            return result
        }

        private data class LinearEquation(val a: Int, val b: Int, val c: Int)

        private data class DoubleEquationSystem(
            val x: Int,
            val y: Int,
            val eq1: LinearEquation,
            val eq2: LinearEquation,
            val d1: Int,
            val d2: Int,
        )

        private fun generateDoubleEquationSystem(
            type: Equations.Type,
            min: Int,
            max: Int,
            withNegative: Boolean,
        ): DoubleEquationSystem {
            repeat(50) {
                val x = pickSolutionValue(min, max)
                val y = pickSolutionValue(min, max)
                if (x == 0 && y == 0) return@repeat

                val equations = pickEquationPair(type, min, max, withNegative, x, y) ?: return@repeat
                return DoubleEquationSystem(
                    x = x,
                    y = y,
                    eq1 = equations.first.equation,
                    eq2 = equations.second.equation,
                    d1 = equations.first.d,
                    d2 = equations.second.d,
                )
            }

            return fallbackDoubleEquationSystem(min, max, type)
        }

        private fun pickSolutionValue(min: Int, max: Int): Int {
            val value = randomNonSame(min, max, 0, 1)
            return if (value == 0 || value == 1) randomNonSame(min, max, 0, 2) else value
        }

        private data class GeneratedEquation(val equation: LinearEquation, val d: Int)

        private fun pickEquationPair(
            type: Equations.Type,
            min: Int,
            max: Int,
            withNegative: Boolean,
            x: Int,
            y: Int,
        ): Pair<GeneratedEquation, GeneratedEquation>? {
            repeat(20) {
                val eq1 = tryRandomLinearEquation(type, min, max, withNegative, x, y) ?: return@repeat
                val eq2 = tryRandomLinearEquation(type, min, max, withNegative, x, y) ?: return@repeat
                if (eq1.equation == eq2.equation) return@repeat
                if (type == Equations.Type.Additional && !hasBothUnknowns(eq1.equation, eq2.equation)) return@repeat
                val det = eq1.equation.b * eq2.equation.c - eq2.equation.b * eq1.equation.c
                if (det != 0) return eq1 to eq2
            }
            return null
        }

        private fun hasBothUnknowns(vararg equations: LinearEquation): Boolean =
            equations.all { it.b != 0 && it.c != 0 }

        private fun tryRandomLinearEquation(
            type: Equations.Type,
            min: Int,
            max: Int,
            withNegative: Boolean,
            x: Int,
            y: Int,
        ): GeneratedEquation? {
            return when (type) {
                Equations.Type.Additional -> {
                    val b = listOf(-1, 1).random()
                    val c = listOf(-1, 1).random()
                    val d = randomNonSame(min, max, 0, if (withNegative) 2 else 5)
                    val a = d - b * x - c * y
                    val aMax = if (withNegative) max else max / 2
                    if (!inRange(a, min, aMax) || (!withNegative && d < 0)) null
                    else GeneratedEquation(LinearEquation(a, b, c), d)
                }

                Equations.Type.Multiplication -> {
                    val limit = maxUniformCoefficient(max, x, y)
                    if (limit < 1) return null
                    repeat(12) {
                        val b = randomNonZeroCoeff(limit, withNegative)
                        val c = randomNonZeroCoeff(limit, withNegative)
                        val d = b * x + c * y
                        if (inRange(d, min, max)) return GeneratedEquation(LinearEquation(0, b, c), d)
                    }
                    null
                }

                Equations.Type.Both -> {
                    val limit = maxUniformCoefficient(max, x, y).coerceAtMost(max / 2)
                    if (limit < 1) return null
                    repeat(12) {
                        val b = randomNonZeroCoeff(limit, withNegative)
                        val c = randomNonZeroCoeff(limit, withNegative)
                        val d = randomNonSame(min, max, 0, if (withNegative) 2 else 5)
                        val a = d - b * x - c * y
                        if (inRange(a, min, max) && inRange(d, min, max)) {
                            return GeneratedEquation(LinearEquation(a, b, c), d)
                        }
                    }
                    null
                }
            }
        }

        private fun fallbackDoubleEquationSystem(
            min: Int,
            max: Int,
            type: Equations.Type,
        ): DoubleEquationSystem {
            repeat(30) {
                val x = pickSolutionValue(min, max)
                val yUpper = (max - x).coerceAtMost(max)
                if (yUpper < min) return@repeat
                val y = randomNonSame(min, yUpper, 0, 2)
                val d1 = x + y
                val d2 = x - y
                if (!inRange(d1, min, max) || !inRange(d2, min, max)) return@repeat
                val eq1 = LinearEquation(a = 0, b = 1, c = 1)
                val eq2 = LinearEquation(a = 0, b = 1, c = -1)
                if (type == Equations.Type.Multiplication || type == Equations.Type.Both) {
                    val limit = maxUniformCoefficient(max, x, y)
                    if (limit < 1) return@repeat
                }
                return DoubleEquationSystem(x, y, eq1, eq2, d1, d2)
            }

            return DoubleEquationSystem(
                x = 5,
                y = 3,
                eq1 = LinearEquation(0, 1, 1),
                eq2 = LinearEquation(0, 1, -1),
                d1 = 8,
                d2 = 2,
            )
        }

        private fun inRange(value: Int, min: Int, max: Int): Boolean = value in min..max

        private fun maxUniformCoefficient(max: Int, x: Int, y: Int): Int {
            val sum = abs(x) + abs(y)
            if (sum == 0) return 0
            return max / sum
        }

        private fun randomNonZeroCoeff(limit: Int, withNegative: Boolean): Int {
            repeat(10) {
                val absValue = randomNonSame(1, limit.coerceAtLeast(1), 0, 3)
                if (absValue == 0) return@repeat
                return if (withNegative && Random.nextBoolean()) -absValue else absValue
            }
            return 1
        }

        private fun formatDoubleEquation(a: Int, b: Int, c: Int, d: Int): String {
            val parts = mutableListOf<String>()
            if (a != 0) parts.add(a.toString())

            if (b != 0) {
                val bPart = if (parts.isEmpty()) {
                    when (b) {
                        1 -> "X"
                        -1 -> "-X"
                        else -> "$b * X"
                    }
                } else {
                    "${if (b > 0) "+" else "-"} ${abs(b).let { coeff -> if (coeff == 1) "X" else "$coeff * X" }}"
                }
                parts.add(bPart)
            }

            if (c != 0) {
                val cPart = if (parts.isEmpty()) {
                    when (c) {
                        1 -> "Y"
                        -1 -> "-Y"
                        else -> "$c * Y"
                    }
                } else {
                    "${if (c > 0) "+" else "-"} ${abs(c).let { coeff -> if (coeff == 1) "Y" else "$coeff * Y" }}"
                }
                parts.add(cPart)
            }

            val left = parts.ifEmpty { listOf("0") }.joinToString(" ")
            return "$left = $d"
        }

        private fun randomNonSame(min: Int, max: Int, target: Int, fraction: Int): Int {

            fun random(min: Int, max: Int, coerceMin: Int, coerceMax: Int, target: Int, fraction: Int, deep: Int = 0): Int {
                val value = ((Random.nextInt(min, max)).toFloat() / fraction).roundToInt().coerceIn(coerceMin, coerceMax)
                return when {
                    abs(value - target) > 3 -> value
                    deep < 10 -> random(
                        min = min,
                        max = max,
                        target = target,
                        fraction = fraction,
                        deep = deep + 1,
                        coerceMax = coerceMax,
                        coerceMin = coerceMin
                    )

                    else -> (target + 1).also {
                        analytics.trackEvent(Event.Action.Logic.RandomBigDeep)
                    }
                }
            }

            val d = max - min
            val tMin = min - d * fraction / 4
            val tMax = max + d * fraction / 4

            return random(min = tMin, max = tMax, target = target, fraction = fraction, coerceMax = max, coerceMin = min)
        }
    }
}

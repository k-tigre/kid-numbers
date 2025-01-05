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

            return when (settings) {
                is GameSettings.Additional -> generateQuestions(settings)
                is GameSettings.Multiplication -> generateQuestions(settings)
                is Equations -> generateQuestions(settings)
            }
        }

        private fun generateQuestions(settings: GameSettings.Multiplication): GameOptions {
            val allQuestions = settings.selectedNumbers
                .flatMap { first ->
                    val questions = (1..10).map { second ->
                        if (settings.isPositive) {
                            if (settings.difficult == Difficult.Hard) {
                                if (Random.nextBoolean()) {
                                    Operation.Multiplication(first = second, second = first)
                                } else {
                                    Operation.Multiplication(first = first, second = second)
                                }
                            } else {
                                Operation.Multiplication(first = first, second = second)
                            }
                        } else {
                            Operation.Division(x = second, second = first)
                        }
                    }
                    when (settings.difficult) {
                        Difficult.Easy -> questions.shuffled()
                        Difficult.Medium -> (questions + questions).shuffled().take(15)
                        Difficult.Hard -> (questions + questions).shuffled()
                    }
                }
                .shuffled()


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
            val allQuestions = (1..count).map {
                val result = Random.nextInt(range.min + range.min, range.max + 1)
                val first = Random.nextInt(range.min, result + 1 - range.min)
                val second = result - first

                if (settings.isPositive) {
                    Operation.Additional(first = first, second = second)
                } else {
                    Operation.Subtraction(x = first, second = second)
                }
            }
                .shuffled(Random)

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
            val questions = (1..count).map {
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
            }

            return questions.shuffled()
        }

        private fun generateSingleEquationsQuestionsForAdditional(
            range: GameSettings.Range,
            count: Int,
        ): List<GameOptions.Question.Equation.Single> {
            // a + X = c
            val min: Int = range.min
            val max: Int = range.max

            val questions = (1..count).map {
                val x: Int
                val a: Int = randomNonSame(min = min, max = if (range.withNegative) max else max / 2, target = 0, fraction = 3)

                val c = randomNonSame(min = if (range.withNegative) min else a, max = (max - a).coerceAtMost(max), target = a, fraction = 1)
                x = c - a

                GameOptions.Question.Equation.Single(
                    x = x,
                    title = "$a + X = $c\nX = %s"
                )
            }

            return questions.shuffled()
        }

        private fun generateSingleEquationsQuestionsForMultiplication(
            range: GameSettings.Range,
            count: Int,
        ): List<GameOptions.Question.Equation.Single> {
            // b * X = c
            val min: Int = range.min
            val max: Int = range.max

            val questions = (1..count).map {
                val b = randomNonSame(min = min / 2, max = max / 2, target = 0, fraction = 3)

                val cTmp = randomNonSame(min = min, max = max, target = min, fraction = 1)
                val x = (cTmp.toFloat() / b).coerceIn(min.toFloat(), range.max.toFloat()).roundToInt()
                val c = b * x

                GameOptions.Question.Equation.Single(
                    x = x,
                    title = "$b * X = $c\nX = %s"
                )
            }

            return questions.shuffled()
        }

        private fun generateDoubleEquationsQuestions(
            ranges: GameSettings.Range,
            count: Int,
            type: Equations.Type
        ): List<GameOptions.Question.Equation.Single> {
            // a1 + b1 * X + c1 * Y = d1
            // a2 + b2 * X + c2 * Y = d2

            // TODO
            return emptyList()
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

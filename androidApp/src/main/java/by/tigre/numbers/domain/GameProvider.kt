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

    class Impl(private val analytics: EventAnalytics) : GameProvider {
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
                        Difficult.Easy -> questions.shuffled(Random)
                        Difficult.Medium -> (questions + questions).shuffled(Random).take(15)
                        Difficult.Hard -> (questions + questions).shuffled(Random)
                    }
                }
                .shuffled(Random)

            val duration = settings.selectedNumbers.size * settings.difficult.time

            return GameOptions(
                questions = allQuestions,
                duration = duration,
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

            val allQuestions = settings.ranges
                .map { type ->
                    (1..count).map {
                        val result = Random.nextInt(type.min + type.min, type.max + 1)
                        val first = Random.nextInt(type.min, result + 1 - type.min)
                        val second = result - first

                        if (settings.isPositive) {
                            Operation.Additional(first = first, second = second)
                        } else {
                            Operation.Subtraction(x = first, second = second)
                        }
                    }
                }
                .flatten()
                .shuffled(Random)

            val duration = settings.ranges.size * settings.difficult.time * 2

            return GameOptions(
                questions = allQuestions,
                duration = duration,
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
                Equations.Dimension.Single -> generateSingleEquationsQuestions(
                    ranges = settings.ranges,
                    count = count,
                    type = settings.type
                )

                Equations.Dimension.Double -> generateDoubleEquationsQuestions(
                    ranges = settings.ranges,
                    count = count,
                    type = settings.type
                )
            }

            val duration = settings.difficult.time * 2

            return GameOptions(
                questions = questions,
                duration = duration,
                difficult = settings.difficult,
                type = GameType.Equations
            )
        }

        private fun generateSingleEquationsQuestions(
            ranges: Equations.Range,
            count: Int,
            type: Equations.Type
        ): List<GameOptions.Question.Equation.Single> {
            // a + b * X = c
            val questions = (1..count).map {
                val x: Int
                val c: Int
                val b: Int = if (type != Equations.Type.Additional) {
                    randomNonSame(min = ranges.min, max = ranges.max, target = 0, fraction = 20)
                } else {
                    1
                }

                val a: Int = if (type != Equations.Type.Multiplication) {
                    randomNonSame(min = ranges.min, max = ranges.max, target = 0, fraction = 10)
                } else {
                    0
                }

                val cTmp = randomNonSame(min = ranges.min, max = ranges.max, target = a, fraction = 4)
                x = ((cTmp - a).toFloat() / b).coerceIn(ranges.min.toFloat(), ranges.max.toFloat()).roundToInt()
                c = a + b * x

                GameOptions.Question.Equation.Single(
                    x = x,
                    title = when (type) {
                        Equations.Type.Additional -> "$a + X = $c\nX = %s"
                        Equations.Type.Multiplication -> "$b * X = $c\nX = %s"
                        Equations.Type.Both -> {
                            "$a ${if (b > 0) "+" else "-"} ${abs(b)} * X = $c\nX = %s"
                        }
                    }
                )
            }

            return questions.shuffled()
        }

        private fun generateDoubleEquationsQuestions(
            ranges: Equations.Range,
            count: Int,
            type: Equations.Type
        ): List<GameOptions.Question.Equation.Single> {
            // a1 + b1 * X + c1 * Y = d1
            // a2 + b2 * X + c2 * Y = d2

            // TODO
            return emptyList()
        }

        private fun randomNonSame(min: Int, max: Int, target: Int, fraction: Int): Int {
            fun random(min: Int, max: Int, target: Int, fraction: Int, deep: Int = 0): Int {
                val value = ((Random.nextInt(min, max)).toFloat() / fraction).roundToInt()
                return when {
                    value != target -> value
                    deep < 10 -> random(min = min, max = max, target = target, fraction = fraction, deep = deep + 1)
                    else -> (target + 1).also {
                        analytics.trackEvent(Event.Action.Logic.RandomBigDeep)
                    }
                }
            }

            val d = max - min
            val tMin = min - d * fraction / 4
            val tMax = max + d * fraction / 4

            return random(min = tMin, max = tMax, target = target, fraction = fraction).coerceIn(min, max)
        }
    }
}

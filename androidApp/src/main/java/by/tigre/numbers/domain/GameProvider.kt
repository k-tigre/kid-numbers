package by.tigre.numbers.domain

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameSettings
import kotlin.random.Random

interface GameProvider {
    fun provide(settings: GameSettings): GameOptions

    class Impl : GameProvider {
        override fun provide(settings: GameSettings): GameOptions {

            return when (settings) {
                is GameSettings.Additional -> generateQuestions(settings)
                is GameSettings.Multiplication -> generateQuestions(settings)
            }
        }

        private fun generateQuestions(settings: GameSettings.Multiplication): GameOptions {
            val allQuestions = settings.selectedNumbers
                .flatMap { first ->
                    val questions = (1..10).map { second ->
                        if (settings.isPositive) {
                            GameOptions.Question.Multiplication(first = first, second = second)
                        } else {
                            GameOptions.Question.Division(result = second, second = first)
                        }
                    }
                    if (settings.difficult == Difficult.Hard) {
                        questions + questions
                    } else {
                        questions
                    }
                }
                .shuffled()

            val duration = settings.selectedNumbers.size * settings.difficult.time

            return GameOptions(
                questions = allQuestions, duration = duration
            )
        }

        private fun generateQuestions(settings: GameSettings.Additional): GameOptions {
            val count: Int = when (settings.difficult) {
                Difficult.Easy -> 10
                Difficult.Medium -> 14
                Difficult.Hard -> 22
            }

            val allQuestions = settings.type.map { type ->
                val (min, max) = when (type) {
                    GameSettings.NumberType.Single -> 0 to 10
                    GameSettings.NumberType.Double -> 10 to 100
                    GameSettings.NumberType.Triples -> 100 to 1000
                    GameSettings.NumberType.SingleDoubleTriples -> 0 to 1000
                    GameSettings.NumberType.SingleDouble -> 0 to 100
                }

                (1..count).map {
                    val result = Random.nextInt(min + min, max + 1)
                    val first = Random.nextInt(min, result + 1 - min)
                    val second = result - first

                    if (settings.isPositive) {
                        GameOptions.Question.Additional(first = first, second = second)
                    } else {
                        GameOptions.Question.Subtraction(result = first, second = second)
                    }
                }
            }.flatten()

            val duration = settings.type.size * settings.difficult.time * 2

            return GameOptions(
                questions = allQuestions, duration = duration
            )
        }

    }
}

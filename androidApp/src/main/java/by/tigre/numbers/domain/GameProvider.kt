package by.tigre.numbers.domain

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
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
                            if (settings.difficult == Difficult.Hard) {
                                if (Random.nextBoolean()) {
                                    GameOptions.Question.Multiplication(first = second, second = first)
                                } else {
                                    GameOptions.Question.Multiplication(first = first, second = second)
                                }
                            } else {
                                GameOptions.Question.Multiplication(first = first, second = second)
                            }
                        } else {
                            GameOptions.Question.Division(result = second, second = first)
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

            val allQuestions = settings.type
                .map { type ->
                    (1..count).map {
                        val result = Random.nextInt(type.min + type.min, type.max + 1)
                        val first = Random.nextInt(type.min, result + 1 - type.min)
                        val second = result - first

                        if (settings.isPositive) {
                            GameOptions.Question.Additional(first = first, second = second)
                        } else {
                            GameOptions.Question.Subtraction(result = first, second = second)
                        }
                    }
                }
                .flatten()
                .shuffled(Random)

            val duration = settings.type.size * settings.difficult.time * 2

            return GameOptions(
                questions = allQuestions,
                duration = duration,
                difficult = settings.difficult,
                type = if (settings.isPositive) GameType.Additional else GameType.Subtraction
            )
        }

    }
}

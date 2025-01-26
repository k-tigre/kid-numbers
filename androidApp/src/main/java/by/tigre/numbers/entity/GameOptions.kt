package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

data class GameOptions(
    val questions: List<Question>,
    val duration: Long,
    val difficult: Difficult,
    val type: GameType
) {

    @Serializable
    sealed interface Question {

        val title: String
        val result: String

        @Serializable
        sealed interface Operation : Question {
            val x: Int

            @Serializable
            data class Multiplication(
                val first: Int,
                val second: Int,
            ) : Operation {
                override val title: String = "$first * $second = %s"
                override val x: Int = first * second
                override val result: String = x.toString()
            }

            @Serializable
            data class Additional(
                val a: Int,
                val b: Int,
            ) : Operation {
                override val title: String = "$a + $b = %s"
                override val x: Int = a + b
                override val result: String = x.toString()
            }

            @Serializable
            data class Division(
                val second: Int,
                override val x: Int,
            ) : Operation {
                val first: Int = x * second // (first / second = result)
                override val title: String = "$first ÷ $second = %s"
                override val result: String = x.toString()
            }

            @Serializable
            data class Subtraction(
                val b: Int,
                override val x: Int,
            ) : Operation {
                val a: Int = x + b // (first - second = result)
                override val title: String = "$a - $b = ?"
                override val result: String = x.toString()
            }
        }

        @Serializable
        sealed interface Equation : Question {
            // A + B * X = C, B * X = C, A + X = C

            @Serializable
            data class Single(
                val x: Int,
                override val title: String
            ) : Equation {
                override val result: String = "X = $x"
            }

            @Serializable
            data class Double(
                val x: Int,
                val y: Int,
                override val title: String
            ) : Equation {
                override val result: String = "X = $x, Y = $y"
            }
        }
    }
}

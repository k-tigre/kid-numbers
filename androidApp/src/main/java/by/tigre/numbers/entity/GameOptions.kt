package by.tigre.numbers.entity

import kotlinx.serialization.SerialName
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
            @SerialName("Multiplication")
            data class Multiplication(
                @SerialName("first")
                val first: Int,
                @SerialName("second")
                val second: Int,
            ) : Operation {
                override val title: String = "$first * $second = %s"
                override val x: Int = first * second
                override val result: String = x.toString()
            }

            @Serializable
            @SerialName("Additional")
            data class Additional(
                @SerialName("a")
                val a: Int,
                @SerialName("b")
                val b: Int,
            ) : Operation {
                override val title: String = "$a + $b = %s"
                override val x: Int = a + b
                override val result: String = x.toString()
            }

            @Serializable
            @SerialName("Division")
            data class Division(
                @SerialName("second")
                val second: Int,
                @SerialName("x")
                override val x: Int,
            ) : Operation {
                val first: Int = x * second // (first / second = result)
                override val title: String = "$first ÷ $second = %s"
                override val result: String = x.toString()
            }

            @Serializable
            @SerialName("Subtraction")
            data class Subtraction(
                @SerialName("b")
                val b: Int,
                @SerialName("x")
                override val x: Int,
            ) : Operation {
                private val a: Int = x + b // (first - second = result)
                override val title: String = "$a - $b = ?"
                override val result: String = x.toString()
            }
        }

        @Serializable
        sealed interface Equation : Question {
            // A + B * X = C, B * X = C, A + X = C

            @Serializable
            @SerialName("Single")
            data class Single(
                @SerialName("x")
                val x: Int,
                @SerialName("title")
                override val title: String
            ) : Equation {
                override val result: String = "X = $x"
            }

            @Serializable
            @SerialName("Double")
            data class Double(
                @SerialName("x")
                val x: Int,
                @SerialName("y")
                val y: Int,
                @SerialName("title")
                override val title: String
            ) : Equation {
                override val result: String = "X = $x, Y = $y"
            }
        }
    }
}

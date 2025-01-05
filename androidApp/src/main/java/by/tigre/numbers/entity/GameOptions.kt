package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

data class GameOptions(
    val questions: List<Question>,
    val duration: Long,
    val difficult: Difficult,
    val type: GameType
) {

    sealed interface Question : Parcelable {

        val title: String
        val result: String

        sealed interface Operation : Question {
            val x: Int

            @Parcelize
            data class Multiplication(
                val first: Int,
                val second: Int,
            ) : Operation {
                @IgnoredOnParcel
                override val title: String = "$first * $second = %s"

                @IgnoredOnParcel
                override val x: Int = first * second

                @IgnoredOnParcel
                override val result: String = x.toString()
            }

            @Parcelize
            data class Additional(
                val a: Int,
                val b: Int,
            ) : Operation {
                @IgnoredOnParcel
                override val title: String = "$a + $b = %s"

                @IgnoredOnParcel
                override val x: Int = a + b

                @IgnoredOnParcel
                override val result: String = x.toString()
            }

            @Parcelize
            data class Division(
                val second: Int,
                override val x: Int,
            ) : Operation {
                @IgnoredOnParcel
                val first: Int = x * second // (first / second = result)

                @IgnoredOnParcel
                override val title: String = "$first ÷ $second = %s"

                @IgnoredOnParcel
                override val result: String = x.toString()
            }

            @Parcelize
            data class Subtraction(
                val b: Int,
                override val x: Int,
            ) : Operation {
                @IgnoredOnParcel
                val a: Int = x + b // (first - second = result)

                @IgnoredOnParcel
                override val title: String = "$a - $b = ?"

                @IgnoredOnParcel
                override val result: String = x.toString()
            }
        }


        sealed interface Equation : Question {
            // A + B * X = C, B * X = C, A + X = C

            @Parcelize
            data class Single(
                val x: Int,
                override val title: String
            ) : Equation {
                @IgnoredOnParcel
                override val result: String = "X = $x"
            }

            @Parcelize
            data class Double(
                val x: Int,
                val y: Int,
                override val title: String
            ) : Equation {
                @IgnoredOnParcel
                override val result: String = "X = $x, Y = $y"
            }

        }
    }
}

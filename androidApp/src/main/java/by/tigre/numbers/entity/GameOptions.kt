package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

data class GameOptions(
    val questions: List<Question>,
    val duration: Long,
    val type: GameType
) {

    sealed interface Question : Parcelable {
        val title: String
        val result: Int

        @Parcelize
        data class Multiplication(
            val first: Int,
            val second: Int,
        ) : Question {
            @IgnoredOnParcel
            override val title: String = "$first * $second"

            @IgnoredOnParcel
            override val result: Int = first * second
        }

        @Parcelize
        data class Additional(
            val first: Int,
            val second: Int,
        ) : Question {
            @IgnoredOnParcel
            override val title: String = "$first + $second"

            @IgnoredOnParcel
            override val result: Int = first + second
        }
    }
}

package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface GameSettings : Parcelable {
    val difficult: Difficult

    @Parcelize
    data class Multiplication(
        val selectedNumbers: List<Int>,
        override val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings

    @Parcelize
    data class Additional(
        val type: List<NumberType>,
        override val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings

    enum class NumberType(val min: Int, val max: Int) {
        Single(0, 10),
        Double(10, 100),
        SingleDouble(0, 100),
        Triples(100, 1000),
        SingleDoubleTriples(0, 1000)
    }
}

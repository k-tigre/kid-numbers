package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface GameSettings : Parcelable {

    @Parcelize
    data class Multiplication(
        val selectedNumbers: List<Int>,
        val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings

//    @Parcelize
//    data class Division(
//        val selectedNumbers: List<Int>,
//        val difficult: Difficult,
//    ) : GameSettings

    @Parcelize
    data class Additional(
        val type: List<NumberType>,
        val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings


//    @Parcelize
//    data class Subtraction(
//        val type: List<NumberType>,
//        val difficult: Difficult,
//    ) : GameSettings

    enum class NumberType {
        Single,
        Double,
        SingleDouble,
        Triples,
        SingleDoubleTriples
    }
}

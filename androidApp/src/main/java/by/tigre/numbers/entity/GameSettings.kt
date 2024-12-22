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
        val type: List<Range>,
        override val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings {
        @Parcelize
        data class Range(val min: Int, val max: Int) : Parcelable
    }
}

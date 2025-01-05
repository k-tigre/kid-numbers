package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
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
        val range: Range,
        override val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings

    @Parcelize
    data class Equations(
        val range: Range,
        override val difficult: Difficult,
        val type: Type,
        val dimension: Dimension
    ) : GameSettings {
        enum class Type {
            Additional, Multiplication, Both
        }

        enum class Dimension {
            Single, Double
        }
    }

    @Parcelize
    data class Range(val max: Int, val withNegative: Boolean) : Parcelable {
        @IgnoredOnParcel
        val min: Int = if (withNegative) -max else 0
    }
}

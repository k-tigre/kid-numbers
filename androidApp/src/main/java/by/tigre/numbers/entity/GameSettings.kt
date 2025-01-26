package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

@Serializable
sealed interface GameSettings {
    val difficult: Difficult

    @Serializable
    data class Multiplication(
        val selectedNumbers: List<Int>,
        override val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings

    @Serializable
    data class Additional(
        val range: Range,
        override val difficult: Difficult,
        val isPositive: Boolean
    ) : GameSettings

    @Serializable
    data class Equations(
        val range: Range,
        override val difficult: Difficult,
        val type: Type,
        val dimension: Dimension
    ) : GameSettings {

        @Serializable
        enum class Type {
            Additional, Multiplication, Both
        }

        @Serializable
        enum class Dimension {
            Single, Double
        }
    }

    @Serializable
    data class Range(val max: Int, val withNegative: Boolean) {
        val min: Int = if (withNegative) -max else 0
    }
}

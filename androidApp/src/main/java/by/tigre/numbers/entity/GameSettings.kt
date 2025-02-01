package by.tigre.numbers.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameSettings {
    val difficult: Difficult

    @Serializable
    @SerialName("Multiplication")
    data class Multiplication(
        @SerialName("selectedNumbers")
        val selectedNumbers: List<Int>,
        @SerialName("difficult")
        override val difficult: Difficult,
        @SerialName("isPositive")
        val isPositive: Boolean
    ) : GameSettings

    @Serializable
    @SerialName("Additional")
    data class Additional(
        @SerialName("range")
        val range: Range,
        @SerialName("difficult")
        override val difficult: Difficult,
        @SerialName("isPositive")
        val isPositive: Boolean
    ) : GameSettings

    @Serializable
    @SerialName("Equations")
    data class Equations(
        @SerialName("range")
        val range: Range,
        @SerialName("difficult")
        override val difficult: Difficult,
        @SerialName("equations_type")
        val type: Type,
        @SerialName("dimension")
        val dimension: Dimension
    ) : GameSettings {

        @Serializable
        enum class Type {
            @SerialName("Additional")
            Additional,

            @SerialName("Multiplication")
            Multiplication,

            @SerialName("Both")
            Both
        }

        @Serializable
        enum class Dimension {
            @SerialName("Single")
            Single,

            @SerialName("Double")
            Double
        }
    }

    @Serializable
    data class Range(
        @SerialName("max")
        val max: Int,
        @SerialName("withNegative")
        val withNegative: Boolean
    ) {
        val min: Int = if (withNegative) -max else 0
    }
}

package by.tigre.numbers.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GameType {
    @SerialName("Additional")
    Additional,

    @SerialName("Subtraction")
    Subtraction,

    @SerialName("Multiplication")
    Multiplication,

    @SerialName("Division")
    Division,

    @SerialName("Equations")
    Equations
}

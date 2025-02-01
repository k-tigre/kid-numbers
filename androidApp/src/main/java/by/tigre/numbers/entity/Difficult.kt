package by.tigre.numbers.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Difficult(val time: Long) {
    @SerialName("Easy")
    Easy(180_000),

    @SerialName("Medium")
    Medium(120_000),

    @SerialName("Hard")
    Hard(90_000)
}
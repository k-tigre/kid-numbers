package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

@Serializable
enum class Difficult(val time: Long) {
    Easy(180_000), Medium(120_000), Hard(90_000)
}
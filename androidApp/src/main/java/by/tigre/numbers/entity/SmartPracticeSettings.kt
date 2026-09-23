package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

@Serializable
data class SmartPracticeSettings(
    val enabledOps: Set<FactKey.Op>,
    val difficult: Difficult = Difficult.Medium,
    val questionCount: Int = 15,
) {
    init {
        require(enabledOps.isNotEmpty()) { "enabledOps must not be empty" }
    }
}

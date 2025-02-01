package by.tigre.numbers.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameResult(
    @SerialName("results")
    val results: List<Result>,
    @SerialName("time")
    val time: Long,
    @SerialName("game_type")
    val type: GameType,
    @SerialName("difficult")
    val difficult: Difficult
) {
    val correctCount by lazy { results.count { it.isCorrect } }
    val inCorrectCount by lazy { results.count { it.isCorrect.not() } }
    val totalCount by lazy { results.size }

    @Serializable
    data class Result(val isCorrect: Boolean, val question: GameOptions.Question, val answer: Int?)
}

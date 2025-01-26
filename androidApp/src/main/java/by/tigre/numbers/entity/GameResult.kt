package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

@Serializable
data class GameResult(
    val results: List<Result>,
    val time: Long,
    val type: GameType,
    val difficult: Difficult
) {
    val correctCount by lazy { results.count { it.isCorrect } }
    val inCorrectCount by lazy { results.count { it.isCorrect.not() } }
    val totalCount by lazy { results.size }

    @Serializable
    data class Result(val isCorrect: Boolean, val question: GameOptions.Question, val answer: Int?)
}

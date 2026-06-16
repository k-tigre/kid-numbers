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
    val difficult: Difficult,
    @SerialName("settings")
    val settings: GameSettings? = null,
) {
    val correctCount by lazy { results.count { it.countsForScore && it.isCorrect } }
    val inCorrectCount by lazy { results.count { it.countsForScore && it.isCorrect.not() } }
    val scoredCount by lazy { results.count { it.countsForScore } }
    val totalCount by lazy { results.size }
    val isPerfectRun by lazy { scoredCount == totalCount && correctCount == totalCount }

    @Serializable
    data class Result(
        val isCorrect: Boolean,
        val question: GameOptions.Question,
        val answer: Int?,
        val answerY: Int? = null,
        @SerialName("counts_for_score")
        val countsForScore: Boolean = true,
    )
}

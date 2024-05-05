package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameResult(
    val results: List<Result>,
    val time: String
) : Parcelable {

    @IgnoredOnParcel
    val correctCount by lazy { results.count { it.isCorrect } }

    @IgnoredOnParcel
    val inCorrectCount by lazy { results.count { it.isCorrect.not() } }

    @IgnoredOnParcel
    val totalCount by lazy { results.size }

    @Parcelize
    data class Result(val isCorrect: Boolean, val question: GameOptions.Question, val answer: Int?) : Parcelable
}

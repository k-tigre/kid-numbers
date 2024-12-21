package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryGameResult(
    val date: Long,
    val duration: Long,
    val difficult: Difficult,
    val gameType: GameType?,
    val correctCount: Int,
    val totalCount: Int
) : Parcelable

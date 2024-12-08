package by.tigre.numbers.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryGameResult(
    val date: Long,
    val duration: String,
    val difficult: Difficult,
    val correctCount: Int,
    val totalCount: Int
) : Parcelable

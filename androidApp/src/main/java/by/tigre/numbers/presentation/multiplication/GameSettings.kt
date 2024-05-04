package by.tigre.numbers.presentation.multiplication

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameSettings(
    val selectedNumbers: List<Int>,
    val difficult: Difficult
) : Parcelable {

    @IgnoredOnParcel
    val totalTime: Long by lazy { selectedNumbers.size * difficult.time }

    enum class Difficult(val time: Long) {
        Easy(300), Medium(120), Hard(90)
    }
}
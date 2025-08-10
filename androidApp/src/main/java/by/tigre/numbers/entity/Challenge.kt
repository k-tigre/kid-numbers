package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: String,
    val tasks: List<Task>,
    val startDate: Long,
    val endDate: Long,
    val duration: Duration,
    val status: Status,
    val isSuccess: Boolean
) {

    @Serializable
    data class Task(val id: Long, val gameSettings: GameSettings, val isCompleted: Boolean)

    enum class Status {
        New, Active, Completed
    }

    enum class Duration(val milliseconds: Long) {
        TenMinutes(10 * 60 * 1000),
        HalfHour(30 * 60 * 1000),
        OneHour(60 * 60 * 1000),
        OneDay(24 * 60 * 60 * 1000),
        OneWeek(7 * 24 * 60 * 60 * 1000)
    }

    companion object {
        const val NO_ID = "NO_ID"
    }
}
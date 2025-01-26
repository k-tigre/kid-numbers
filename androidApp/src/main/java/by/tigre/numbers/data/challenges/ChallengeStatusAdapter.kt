package by.tigre.numbers.data.challenges

import app.cash.sqldelight.ColumnAdapter
import by.tigre.numbers.entity.Challenge

object ChallengeStatusAdapter : ColumnAdapter<Challenge.Status, Long> {
    override fun decode(databaseValue: Long): Challenge.Status = when (databaseValue) {
        0L -> Challenge.Status.New
        1L -> Challenge.Status.Active
        else -> Challenge.Status.Completed
    }

    override fun encode(value: Challenge.Status): Long = when (value) {
        Challenge.Status.New -> 0L
        Challenge.Status.Active -> 1L
        Challenge.Status.Completed -> 2L
    }
}
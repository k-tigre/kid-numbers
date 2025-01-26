package by.tigre.numbers.data.challenges

import androidx.compose.ui.util.fastFirstOrNull
import app.cash.sqldelight.ColumnAdapter
import by.tigre.numbers.entity.Challenge

object ChallengeDurationAdapter : ColumnAdapter<Challenge.Duration, Long> {
    override fun decode(databaseValue: Long): Challenge.Duration = Challenge.Duration.entries
        .fastFirstOrNull { it.milliseconds == databaseValue } ?: Challenge.Duration.TenMinutes

    override fun encode(value: Challenge.Duration): Long = value.milliseconds
}
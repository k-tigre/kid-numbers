package by.tigre.numbers.data.history

import app.cash.sqldelight.ColumnAdapter
import by.tigre.numbers.entity.Difficult

object StoreDifficultAdapter : ColumnAdapter<Difficult, Long> {
    override fun decode(databaseValue: Long) = when (databaseValue) {
        0L -> Difficult.Easy
        10L -> Difficult.Medium
        else -> Difficult.Hard
    }

    override fun encode(value: Difficult) = when (value) {
        Difficult.Easy -> 0L
        Difficult.Medium -> 10L
        Difficult.Hard -> 20L
    }
}
package by.tigre.numbers.data.history

import app.cash.sqldelight.ColumnAdapter
import by.tigre.numbers.entity.GameType

object StoreGameTypeAdapter : ColumnAdapter<GameType, Long> {
    override fun decode(databaseValue: Long) = when (databaseValue) {
        0L -> GameType.Additional
        1L -> GameType.Division
        2L -> GameType.Multiplication
        else -> GameType.Subtraction
    }

    override fun encode(value: GameType) = when (value) {
        GameType.Additional -> 0L
        GameType.Division -> 1L
        GameType.Multiplication -> 2L
        GameType.Subtraction -> 3L
    }
}
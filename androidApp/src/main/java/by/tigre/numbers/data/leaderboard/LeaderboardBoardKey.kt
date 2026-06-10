package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.GameSettings

object LeaderboardBoardKey {

    fun from(settings: GameSettings): String = when (settings) {
        is GameSettings.Multiplication -> {
            val operation: String = if (settings.isPositive) "Mult" else "Div"
            val numbers: String = settings.selectedNumbers.sorted().joinToString("-")
            "${operation}_${numbers}_${settings.difficult.name}"
        }
        is GameSettings.Additional -> {
            val operation: String = if (settings.isPositive) "Add" else "Sub"
            val sign: String = if (settings.range.withNegative) "n" else "p"
            "${operation}_${settings.range.max}_${sign}_${settings.difficult.name}"
        }
        is GameSettings.Equations -> {
            val sign: String = if (settings.range.withNegative) "n" else "p"
            "Eq_${settings.range.max}_${sign}_${settings.dimension.name}_${settings.type.name}_${settings.difficult.name}"
        }
    }

    fun documentId(boardKey: String, userId: String): String = "${boardKey}_$userId"
}

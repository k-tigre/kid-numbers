package by.tigre.numbers.leaderboard

import by.tigre.numbers.data.leaderboard.LeaderboardBoardKey
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class LeaderboardBoardKeyTest {

    @Test
    fun multiplicationKeyIsStable() {
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(10, 2, 5),
            difficult = Difficult.Easy,
            isPositive = true,
        )
        assertEquals("Mult_2-5-10_Easy", LeaderboardBoardKey.from(settings))
    }

    @Test
    fun additionalKeyIncludesRangeSign() {
        val settings: GameSettings.Additional = GameSettings.Additional(
            range = GameSettings.Range(max = 100, withNegative = true),
            difficult = Difficult.Medium,
            isPositive = false,
        )
        assertEquals("Sub_100_n_Medium", LeaderboardBoardKey.from(settings))
    }
}

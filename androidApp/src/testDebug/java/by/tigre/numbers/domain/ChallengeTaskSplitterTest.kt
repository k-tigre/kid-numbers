package by.tigre.numbers.domain

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ChallengeTaskSplitterTest {

    @Test
    fun multiplicationGroupSizes() {
        assertEquals(listOf(3), ChallengeTaskSplitter.multiplicationGroupSizes(3))
        assertEquals(listOf(2, 2), ChallengeTaskSplitter.multiplicationGroupSizes(4))
        assertEquals(listOf(2, 3), ChallengeTaskSplitter.multiplicationGroupSizes(5))
        assertEquals(listOf(2, 2, 2), ChallengeTaskSplitter.multiplicationGroupSizes(6))
        assertEquals(listOf(2, 2, 3), ChallengeTaskSplitter.multiplicationGroupSizes(7))
    }

    @Test
    fun splitKeepsSingleTaskForUpToThreeNumbers() {
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(2, 5, 9),
            difficult = Difficult.Medium,
            isPositive = true,
        )
        val result: List<GameSettings> = ChallengeTaskSplitter.split(settings, Random(0))
        assertEquals(1, result.size)
        assertEquals(listOf(2, 5, 9), (result.first() as GameSettings.Multiplication).selectedNumbers)
    }

    @Test
    fun splitSixNumbersIntoThreeTasksOfTwo() {
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(1, 2, 3, 4, 5, 6),
            difficult = Difficult.Hard,
            isPositive = false,
        )
        val result: List<GameSettings.Multiplication> = ChallengeTaskSplitter.split(settings, Random(42))
            .map { it as GameSettings.Multiplication }
        assertEquals(3, result.size)
        assertEquals(listOf(2, 2, 2), result.map { it.selectedNumbers.size })
        assertEquals(listOf(1, 2, 3, 4, 5, 6), result.flatMap { it.selectedNumbers }.sorted())
        result.forEach { task ->
            assertEquals(Difficult.Hard, task.difficult)
            assertEquals(false, task.isPositive)
        }
    }

    @Test
    fun splitFiveNumbersIntoTwoAndThree() {
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(1, 2, 3, 4, 5),
            difficult = Difficult.Easy,
            isPositive = true,
        )
        val result: List<GameSettings.Multiplication> = ChallengeTaskSplitter.split(settings, Random(7))
            .map { it as GameSettings.Multiplication }
        assertEquals(2, result.size)
        assertEquals(listOf(2, 3), result.map { it.selectedNumbers.size }.sorted())
        assertEquals(listOf(1, 2, 3, 4, 5), result.flatMap { it.selectedNumbers }.sorted())
    }

    @Test
    fun splitDoesNotChangeOtherGameSettings() {
        val settings: GameSettings.Additional = GameSettings.Additional(
            range = GameSettings.Range(max = 100, withNegative = false),
            difficult = Difficult.Medium,
            isPositive = true,
        )
        val result: List<GameSettings> = ChallengeTaskSplitter.split(settings, Random(0))
        assertEquals(listOf(settings), result)
    }
}

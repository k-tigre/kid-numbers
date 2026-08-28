package by.tigre.numbers.domain

import by.tigre.numbers.entity.GameSettings
import kotlin.random.Random

object ChallengeTaskSplitter {
    fun split(settings: GameSettings, random: Random = Random): List<GameSettings> = when (settings) {
        is GameSettings.Multiplication -> splitMultiplication(settings, random)
        else -> listOf(settings)
    }

    private fun splitMultiplication(
        settings: GameSettings.Multiplication,
        random: Random,
    ): List<GameSettings> {
        val numbers: List<Int> = settings.selectedNumbers
        if (numbers.size <= 3) {
            return listOf(settings)
        }
        val groupSizes: List<Int> = multiplicationGroupSizes(numbers.size).shuffled(random)
        val shuffledNumbers: List<Int> = numbers.shuffled(random)
        var index: Int = 0
        return groupSizes.map { size ->
            val chunk: List<Int> = shuffledNumbers.subList(index, index + size)
            index += size
            settings.copy(selectedNumbers = chunk)
        }
    }

    internal fun multiplicationGroupSizes(count: Int): List<Int> {
        if (count <= 3) {
            return listOf(count)
        }
        return if (count % 2 == 0) {
            List(count / 2) { 2 }
        } else {
            List((count - 3) / 2) { 2 } + listOf(3)
        }
    }
}

package by.tigre.numbers.domain

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameProviderTest {

    private val provider: GameProvider = GameProvider.Impl(
        analytics = NoOpEventAnalytics,
        durationProvider = FakeGameDurationProvider,
    )

    @Test
    fun generatedQuestionsHaveNoConsecutiveDuplicates() {
        val settingsVariants: List<GameSettings> = listOf(
            GameSettings.Multiplication(
                selectedNumbers = listOf(3, 7),
                difficult = Difficult.Medium,
                isPositive = true,
            ),
            GameSettings.Multiplication(
                selectedNumbers = listOf(5),
                difficult = Difficult.Hard,
                isPositive = false,
            ),
            GameSettings.Additional(
                range = GameSettings.Range(max = 100, withNegative = false),
                difficult = Difficult.Hard,
                isPositive = true,
            ),
            GameSettings.Additional(
                range = GameSettings.Range(max = 50, withNegative = true),
                difficult = Difficult.Medium,
                isPositive = false,
            ),
            Equations(
                range = GameSettings.Range(max = 50, withNegative = false),
                difficult = Difficult.Hard,
                type = Equations.Type.Additional,
                dimension = Equations.Dimension.Single,
            ),
            Equations(
                range = GameSettings.Range(max = 100, withNegative = true),
                difficult = Difficult.Medium,
                type = Equations.Type.Both,
                dimension = Equations.Dimension.Double,
            ),
        )
        repeat(20) {
            settingsVariants.forEach { settings ->
                assertNoConsecutiveDuplicates(provider.provide(settings).questions)
            }
        }
    }

    @Test
    fun multiplicationAllowsRepeatedQuestionsWhenPoolIsSmallerThanCount() {
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(5),
            difficult = Difficult.Medium,
            isPositive = true,
        )
        val titles: List<String> = provider.provide(settings).questions.map { it.title }
        assertTrue(titles.size > titles.toSet().size)
        assertNoConsecutiveDuplicates(provider.provide(settings).questions)
    }

    private fun assertNoConsecutiveDuplicates(questions: List<GameOptions.Question>) {
        questions.map { it.title }.zipWithNext { previous, current ->
            assertNotEquals(previous, current)
        }
    }

    private object NoOpEventAnalytics : EventAnalytics {
        override fun trackEvent(event: Event.Action) = Unit
    }

    private object FakeGameDurationProvider : GameDurationProvider {
        override fun provide(settings: GameSettings): Long = 60_000L
    }
}

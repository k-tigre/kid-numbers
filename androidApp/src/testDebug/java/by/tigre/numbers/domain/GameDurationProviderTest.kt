package by.tigre.numbers.domain

import by.tigre.numbers.data.remoteconfig.GameDurationConfig
import by.tigre.numbers.data.remoteconfig.RemoteConfigKeys
import by.tigre.numbers.data.remoteconfig.RemoteConfigProvider
import by.tigre.numbers.data.remoteconfig.parseGameDurationConfig
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDurationProviderTest {

    private val provider: GameDurationProvider = GameDurationProvider.Impl(
        provider = FakeRemoteConfigProvider(
            values = mapOf(RemoteConfigKeys.GAME_DURATION_JSON to GameDurationConfig.DEFAULT_JSON),
        ),
    )

    @Test
    fun defaultJsonMatchesGameDurationConfigDefault() {
        val parsed: GameDurationConfig = parseGameDurationConfig(GameDurationConfig.DEFAULT_JSON)
        assertEquals(GameDurationConfig.DEFAULT, parsed)
    }

    @Test
    fun defaultConfigMatchesLegacyHardcodedDurations() {
        val rangeMaxValues: List<Int> = listOf(10, 100, 1000, 5000)
        val digitCounts: List<Int> = listOf(1, 3, 5)

        Difficult.entries.forEach { difficult ->
            digitCounts.forEach { digitCount ->
                val multiplication: GameSettings.Multiplication = GameSettings.Multiplication(
                    selectedNumbers = (1..digitCount).toList(),
                    difficult = difficult,
                    isPositive = true,
                )
                assertLegacyMatch(multiplication)
            }

            rangeMaxValues.forEach { max ->
                val addition: GameSettings.Additional = GameSettings.Additional(
                    range = GameSettings.Range(max = max, withNegative = false),
                    difficult = difficult,
                    isPositive = true,
                )
                assertLegacyMatch(addition)

                val additionNegative: GameSettings.Additional = GameSettings.Additional(
                    range = GameSettings.Range(max = max, withNegative = true),
                    difficult = difficult,
                    isPositive = true,
                )
                assertLegacyMatch(additionNegative)

                Equations.Type.entries.forEach { type ->
                    Equations.Dimension.entries.forEach { dimension ->
                        val equations: GameSettings.Equations = GameSettings.Equations(
                            range = GameSettings.Range(max = max, withNegative = max > 100),
                            difficult = difficult,
                            type = type,
                            dimension = dimension,
                        )
                        assertLegacyMatch(equations)
                    }
                }
            }
        }
    }

    private fun assertLegacyMatch(settings: GameSettings) {
        assertEquals(
            LegacyGameDuration.provide(settings),
            provider.provide(settings),
        )
    }

    @Test
    fun multiplicationUsesDigitCountTimesBaseTime() {
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(2, 5, 10),
            difficult = Difficult.Medium,
            isPositive = true,
        )

        assertEquals(3 * Difficult.Medium.time, provider.provide(settings))
    }

    @Test
    fun additionUsesRangeMultiplier() {
        val settings: GameSettings.Additional = GameSettings.Additional(
            range = GameSettings.Range(max = 100, withNegative = false),
            difficult = Difficult.Medium,
            isPositive = true,
        )

        assertEquals(Difficult.Medium.time, provider.provide(settings))
    }

    @Test
    fun remoteConfigOverridesBaseTime() {
        val customProvider: GameDurationProvider = GameDurationProvider.Impl(
            provider = FakeRemoteConfigProvider(
                values = mapOf(
                    RemoteConfigKeys.GAME_DURATION_JSON to """{"baseTimeMs":60000}""",
                ),
            ),
        )
        val settings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(2),
            difficult = Difficult.Medium,
            isPositive = true,
        )

        assertEquals(60_000L, customProvider.provide(settings))
    }

    @Test
    fun remoteConfigOverridesAdditionRangeMultipliers() {
        val customProvider: GameDurationProvider = GameDurationProvider.Impl(
            provider = FakeRemoteConfigProvider(
                values = mapOf(
                    RemoteConfigKeys.GAME_DURATION_JSON to
                        """{"additionRangeMultipliers":{"upTo10":1,"upTo100":1.3,"upTo1000":1.7,"above1000":2.1}}""",
                ),
            ),
        )
        val smallRange: GameSettings.Additional = GameSettings.Additional(
            range = GameSettings.Range(max = 10, withNegative = false),
            difficult = Difficult.Medium,
            isPositive = true,
        )
        val mediumRange: GameSettings.Additional = GameSettings.Additional(
            range = GameSettings.Range(max = 100, withNegative = false),
            difficult = Difficult.Medium,
            isPositive = true,
        )

        assertEquals(120_000L, customProvider.provide(smallRange))
        assertEquals(156_000L, customProvider.provide(mediumRange))
    }

    @Test
    fun equationSingleUsesTypeMultiplierFromRemoteConfig() {
        val customProvider: GameDurationProvider = GameDurationProvider.Impl(
            provider = FakeRemoteConfigProvider(
                values = mapOf(
                    RemoteConfigKeys.GAME_DURATION_JSON to
                        """{"equationSingleTypeMultipliers":{"additional":1,"multiplication":1.2,"both":2}}""",
                ),
            ),
        )
        val settings: GameSettings.Equations = GameSettings.Equations(
            range = GameSettings.Range(max = 50, withNegative = false),
            difficult = Difficult.Medium,
            type = GameSettings.Equations.Type.Both,
            dimension = GameSettings.Equations.Dimension.Single,
        )

        assertEquals(360_000L, customProvider.provide(settings))
    }

    private class FakeRemoteConfigProvider(
        private val values: Map<String, String> = emptyMap(),
    ) : RemoteConfigProvider {
        override suspend fun refresh(): Boolean = false

        override fun getString(key: String, default: String): String = values[key] ?: default

        override fun getBoolean(key: String, default: Boolean): Boolean = default
    }
}

/** Pre–Remote Config duration logic kept for regression checks. */
private object LegacyGameDuration {
    fun provide(settings: GameSettings): Long {
        return when (settings) {
            is GameSettings.Additional -> {
                val rangeSize: Int = abs(settings.range.max - settings.range.min)
                val rangeMultiplication: Float = when {
                    rangeSize < 11 -> 0.5f
                    rangeSize < 101 -> 1f
                    rangeSize < 1001 -> 2f
                    else -> 3f
                }
                (settings.difficult.time * rangeMultiplication).toLong()
            }

            is GameSettings.Multiplication -> settings.selectedNumbers.size * settings.difficult.time

            is Equations -> {
                val rangeSize: Int = abs(settings.range.max - settings.range.min)
                val isSmallRange: Boolean = rangeSize < 101

                when (settings.dimension) {
                    Equations.Dimension.Double -> {
                        val rangeMultiplication: Float = if (isSmallRange) 2f else 3f
                        val typeMultiplication: Float = when (settings.type) {
                            Equations.Type.Both -> 2f
                            Equations.Type.Additional, Equations.Type.Multiplication -> 1f
                        }
                        (settings.difficult.time * rangeMultiplication * typeMultiplication).toLong()
                    }

                    Equations.Dimension.Single -> {
                        val rangeMultiplication: Float = when {
                            rangeSize < 101 -> 1.5f
                            rangeSize < 501 -> 2f
                            rangeSize < 1001 -> 2.5f
                            else -> 3f
                        }
                        val typeMultiplication: Float = when (settings.type) {
                            Equations.Type.Additional, Equations.Type.Multiplication -> 1f
                            Equations.Type.Both -> 1.5f
                        }
                        (settings.difficult.time * rangeMultiplication * typeMultiplication).toLong()
                    }
                }
            }
        }
    }
}

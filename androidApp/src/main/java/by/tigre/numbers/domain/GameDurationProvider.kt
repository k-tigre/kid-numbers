package by.tigre.numbers.domain

import by.tigre.numbers.data.remoteconfig.GameDurationConfig
import by.tigre.numbers.data.remoteconfig.RemoteConfigKeys
import by.tigre.numbers.data.remoteconfig.RemoteConfigProvider
import by.tigre.numbers.data.remoteconfig.parseGameDurationConfig
import by.tigre.numbers.data.remoteconfig.rangeSize
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations

interface GameDurationProvider {
    fun provide(settings: GameSettings): Long

    class Impl(
        private val provider: RemoteConfigProvider,
    ) : GameDurationProvider {

        override fun provide(settings: GameSettings): Long {
            val config: GameDurationConfig = readConfig()
            val baseTime: Long = config.baseTimeFor(settings.difficult)

            return when (settings) {
                is GameSettings.Additional -> {
                    val size: Int = rangeSize(settings.range.max, settings.range.min)
                    (baseTime * config.additionRangeMultiplier(size)).toLong()
                }

                is GameSettings.Multiplication -> settings.selectedNumbers.size * baseTime

                is Equations -> {
                    val size: Int = rangeSize(settings.range.max, settings.range.min)

                    when (settings.dimension) {
                        Equations.Dimension.Double -> {
                            val rangeMultiplication: Float = config.equationDoubleRangeMultiplier(size)
                            val typeMultiplication: Float = config.equationDoubleTypeMultiplier(settings.type)
                            (baseTime * rangeMultiplication * typeMultiplication).toLong()
                        }

                        Equations.Dimension.Single -> {
                            val rangeMultiplication: Float = config.equationSingleRangeMultiplier(size)
                            val typeMultiplication: Float = config.equationSingleTypeMultiplier(settings.type)
                            (baseTime * rangeMultiplication * typeMultiplication).toLong()
                        }
                    }
                }
            }
        }

        private fun readConfig(): GameDurationConfig {
            val json: String = provider.getString(RemoteConfigKeys.GAME_DURATION_JSON, default = "")
            return parseGameDurationConfig(json)
        }
    }
}

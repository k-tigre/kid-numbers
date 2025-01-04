package by.tigre.numbers.domain

import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import kotlin.math.abs

interface GameDurationProvider {
    fun provide(settings: GameSettings): Long

    class Impl : GameDurationProvider {
        override fun provide(settings: GameSettings): Long {

            return when (settings) {
                is GameSettings.Additional -> settings.ranges.size * settings.difficult.time
                is GameSettings.Multiplication -> settings.selectedNumbers.size * settings.difficult.time
                is Equations -> {
                    val rangeSize = abs(settings.ranges.max - settings.ranges.min)

                    val rangeMultiplication = when {
                        rangeSize < 101 -> 1f
                        rangeSize < 501 -> 1.5f
                        rangeSize < 1001 -> 2f
                        else -> 3f
                    }
                    val typeMultiplication = when (settings.type) {
                        Equations.Type.Additional -> 1f
                        Equations.Type.Multiplication -> 1f
                        Equations.Type.Both -> 1.5f
                    }
                    (settings.difficult.time * rangeMultiplication * typeMultiplication).toLong()
                }
            }
        }
    }
}

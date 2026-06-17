package by.tigre.numbers.data.remoteconfig

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings.Equations
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs

@Serializable
data class GameDurationConfig(
    val baseTimeMs: Long = DEFAULT.baseTimeMs,
    val additionRangeMultipliers: AdditionRangeMultipliers = DEFAULT.additionRangeMultipliers,
    val equationSingleRangeMultipliers: EquationSingleRangeMultipliers = DEFAULT.equationSingleRangeMultipliers,
    val equationSingleTypeMultipliers: EquationTypeMultipliers = DEFAULT.equationSingleTypeMultipliers,
    val equationDoubleRangeMultipliers: EquationDoubleRangeMultipliers = DEFAULT.equationDoubleRangeMultipliers,
    val equationDoubleTypeMultipliers: EquationTypeMultipliers = DEFAULT.equationDoubleTypeMultipliers,
) {
    fun baseTimeFor(difficult: Difficult): Long {
        return (baseTimeMs * difficult.time.toDouble() / Difficult.Medium.time).toLong()
    }

    fun additionRangeMultiplier(rangeSize: Int): Float {
        return additionRangeMultipliers.forRangeSize(rangeSize)
    }

    fun equationSingleRangeMultiplier(rangeSize: Int): Float {
        return equationSingleRangeMultipliers.forRangeSize(rangeSize)
    }

    fun equationDoubleRangeMultiplier(rangeSize: Int): Float {
        return equationDoubleRangeMultipliers.forRangeSize(rangeSize)
    }

    fun equationSingleTypeMultiplier(type: Equations.Type): Float {
        return equationSingleTypeMultipliers.forType(type)
    }

    fun equationDoubleTypeMultiplier(type: Equations.Type): Float {
        return equationDoubleTypeMultipliers.forType(type)
    }

    companion object {
        val DEFAULT: GameDurationConfig = GameDurationConfig(
            baseTimeMs = Difficult.Medium.time,
            additionRangeMultipliers = AdditionRangeMultipliers.DEFAULT,
            equationSingleRangeMultipliers = EquationSingleRangeMultipliers.DEFAULT,
            equationSingleTypeMultipliers = EquationTypeMultipliers(
                additional = 1f,
                multiplication = 1f,
                both = 1.5f,
            ),
            equationDoubleRangeMultipliers = EquationDoubleRangeMultipliers.DEFAULT,
            equationDoubleTypeMultipliers = EquationTypeMultipliers(
                additional = 1f,
                multiplication = 1f,
                both = 2f,
            ),
        )
        const val DEFAULT_JSON: String =
            """{"baseTimeMs":120000,"additionRangeMultipliers":{"upTo10":0.5,"upTo100":1,"upTo1000":2,"above1000":3},"equationSingleRangeMultipliers":{"upTo100":1.5,"upTo500":2,"upTo1000":2.5,"above1000":3},"equationSingleTypeMultipliers":{"additional":1,"multiplication":1,"both":1.5},"equationDoubleRangeMultipliers":{"upTo100":2,"above100":3},"equationDoubleTypeMultipliers":{"additional":1,"multiplication":1,"both":2}}"""
    }
}

@Serializable
data class AdditionRangeMultipliers(
    val upTo10: Float = DEFAULT.upTo10,
    val upTo100: Float = DEFAULT.upTo100,
    val upTo1000: Float = DEFAULT.upTo1000,
    val above1000: Float = DEFAULT.above1000,
) {
    fun forRangeSize(rangeSize: Int): Float = when {
        rangeSize < 11 -> upTo10
        rangeSize < 101 -> upTo100
        rangeSize < 1001 -> upTo1000
        else -> above1000
    }

    companion object {
        val DEFAULT: AdditionRangeMultipliers = AdditionRangeMultipliers(
            upTo10 = 0.5f,
            upTo100 = 1f,
            upTo1000 = 2f,
            above1000 = 3f,
        )
    }
}

@Serializable
data class EquationSingleRangeMultipliers(
    val upTo100: Float = DEFAULT.upTo100,
    val upTo500: Float = DEFAULT.upTo500,
    val upTo1000: Float = DEFAULT.upTo1000,
    val above1000: Float = DEFAULT.above1000,
) {
    fun forRangeSize(rangeSize: Int): Float = when {
        rangeSize < 101 -> upTo100
        rangeSize < 501 -> upTo500
        rangeSize < 1001 -> upTo1000
        else -> above1000
    }

    companion object {
        val DEFAULT: EquationSingleRangeMultipliers = EquationSingleRangeMultipliers(
            upTo100 = 1.5f,
            upTo500 = 2f,
            upTo1000 = 2.5f,
            above1000 = 3f,
        )
    }
}

@Serializable
data class EquationDoubleRangeMultipliers(
    val upTo100: Float = DEFAULT.upTo100,
    val above100: Float = DEFAULT.above100,
) {
    fun forRangeSize(rangeSize: Int): Float = if (rangeSize < 101) upTo100 else above100

    companion object {
        val DEFAULT: EquationDoubleRangeMultipliers = EquationDoubleRangeMultipliers(
            upTo100 = 2f,
            above100 = 3f,
        )
    }
}

@Serializable
data class EquationTypeMultipliers(
    val additional: Float = 1f,
    val multiplication: Float = 1f,
    val both: Float = 1f,
) {
    fun forType(type: Equations.Type): Float = when (type) {
        Equations.Type.Additional -> additional
        Equations.Type.Multiplication -> multiplication
        Equations.Type.Both -> both
    }
}

private val durationJson: Json = Json { ignoreUnknownKeys = true }

fun parseGameDurationConfig(json: String): GameDurationConfig {
    if (json.isBlank()) return GameDurationConfig.DEFAULT
    return runCatching {
        durationJson.decodeFromString<GameDurationConfig>(json)
    }.getOrDefault(GameDurationConfig.DEFAULT)
}

internal fun rangeSize(max: Int, min: Int): Int = abs(max - min)

package by.tigre.numbers.domain

import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.domain.facts.FactCurriculum
import by.tigre.numbers.domain.facts.SmartSessionBuilder
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.SmartPracticeSettings

interface SmartGameProvider {
    suspend fun provide(settings: SmartPracticeSettings, nowMs: Long = System.currentTimeMillis()): GameOptions

    class Impl(
        private val factStore: FactStore,
        private val durationProvider: GameDurationProvider,
        private val sessionBuilder: SmartSessionBuilder = SmartSessionBuilder(),
    ) : SmartGameProvider {
        override suspend fun provide(settings: SmartPracticeSettings, nowMs: Long): GameOptions {
            val enabledOps: Set<FactKey.Op> = settings.enabledOps
            require(enabledOps.isNotEmpty()) { "enabledOps must not be empty" }
            val stats: List<FactStats> = factStore.getByOps(enabledOps)
            val universe: Set<FactKey> = FactCurriculum.selectableUniverse(enabledOps, stats)
            factStore.ensureKeys(universe)
            val keys: List<FactKey> = sessionBuilder.build(stats, universe, settings.questionCount, nowMs)
            val questions: List<Operation> = keys.map { key -> key.toOperation() }
            // Duration uses synthetic Multiplication(2..5) — same Medium baseline regardless of enabled ops.
            val durationSettings: GameSettings.Multiplication = GameSettings.Multiplication(
                selectedNumbers = listOf(2, 3, 4, 5),
                difficult = settings.difficult,
                isPositive = true,
            )
            val duration: Long = durationProvider.provide(durationSettings)
            val type: GameType = resolveGameType(keys)
            return GameOptions(
                questions = questions,
                duration = duration,
                difficult = settings.difficult,
                type = type,
            )
        }

        private fun resolveGameType(keys: List<FactKey>): GameType {
            if (keys.isEmpty()) {
                return GameType.Multiplication
            }
            val majorityOp: FactKey.Op = keys
                .groupingBy { it.op }
                .eachCount()
                .maxBy { it.value }
                .key
            return majorityOp.toGameType()
        }

        private fun FactKey.toOperation(): Operation {
            return when (op) {
                FactKey.Op.ADD -> Operation.Additional(a = a, b = b)
                FactKey.Op.SUB -> Operation.Subtraction(b = b, x = a - b)
                FactKey.Op.MUL -> Operation.Multiplication(first = a, second = b)
                FactKey.Op.DIV -> Operation.Division(second = b, x = a / b)
            }
        }

        private fun FactKey.Op.toGameType(): GameType {
            return when (this) {
                FactKey.Op.ADD -> GameType.Additional
                FactKey.Op.SUB -> GameType.Subtraction
                FactKey.Op.MUL -> GameType.Multiplication
                FactKey.Op.DIV -> GameType.Division
            }
        }
    }
}

package by.tigre.numbers.domain

import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.SmartPracticeSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartGameProviderTest {

    private val durationProvider: GameDurationProvider = FakeGameDurationProvider

    @Test
    fun provideReturnsRequestedCountForSingleOp() = runBlocking {
        val enabledOps: Set<FactKey.Op> = setOf(FactKey.Op.MUL)
        val provider: SmartGameProvider = SmartGameProvider.Impl(
            factStore = FakeFactStore(),
            durationProvider = durationProvider,
        )
        val options: GameOptions = provider.provide(
            settings = SmartPracticeSettings(
                enabledOps = enabledOps,
                difficult = Difficult.Medium,
                questionCount = 10,
            ),
            nowMs = 1_000_000L,
        )
        assertEquals(10, options.questions.size)
        assertEquals(Difficult.Medium, options.difficult)
        assertEquals(60_000L, options.duration)
        options.questions.forEach { question ->
            val operation: GameOptions.Question.Operation = question as GameOptions.Question.Operation
            val key: FactKey = FactKey.fromQuestion(operation)!!
            assertTrue(enabledOps.contains(key.op))
        }
    }

    @Test
    fun provideReturnsOnlyEnabledOps() = runBlocking {
        val enabledOps: Set<FactKey.Op> = setOf(FactKey.Op.MUL, FactKey.Op.ADD)
        val provider: SmartGameProvider = SmartGameProvider.Impl(
            factStore = FakeFactStore(),
            durationProvider = durationProvider,
        )
        val options: GameOptions = provider.provide(
            settings = SmartPracticeSettings(enabledOps = enabledOps, questionCount = 10),
            nowMs = 1_000_000L,
        )
        assertTrue(options.questions.isNotEmpty())
        options.questions.forEach { question ->
            val operation: GameOptions.Question.Operation = question as GameOptions.Question.Operation
            val key: FactKey = FactKey.fromQuestion(operation)!!
            assertTrue(enabledOps.contains(key.op))
        }
    }

    @Test
    fun provideMapsSubtractionAndDivisionFieldsCorrectly() = runBlocking {
        val enabledOps: Set<FactKey.Op> = setOf(FactKey.Op.SUB, FactKey.Op.DIV)
        val provider: SmartGameProvider = SmartGameProvider.Impl(
            factStore = FakeFactStore(),
            durationProvider = durationProvider,
        )
        val options: GameOptions = provider.provide(
            settings = SmartPracticeSettings(
                enabledOps = enabledOps,
                questionCount = 10,
            ),
            nowMs = 1_000_000L,
        )
        assertTrue(options.questions.isNotEmpty())
        options.questions.forEach { question ->
            val operation: GameOptions.Question.Operation = question as GameOptions.Question.Operation
            val key: FactKey = FactKey.fromQuestion(operation)!!
            assertTrue(enabledOps.contains(key.op))
            when (operation) {
                is GameOptions.Question.Operation.Subtraction -> {
                    assertEquals(key.b, operation.b)
                    assertEquals(key.a - key.b, operation.x)
                }
                is GameOptions.Question.Operation.Division -> {
                    assertEquals(key.b, operation.second)
                    assertEquals(key.a / key.b, operation.x)
                }
                else -> error("Expected SUB or DIV, got ${key.op}")
            }
        }
    }

    private object FakeGameDurationProvider : GameDurationProvider {
        override fun provide(settings: GameSettings): Long = 60_000L
    }

    private class FakeFactStore : FactStore {
        override suspend fun get(key: FactKey): FactStats? = null
        override suspend fun getByOp(op: FactKey.Op): List<FactStats> = emptyList()
        override suspend fun getByOps(ops: Set<FactKey.Op>): List<FactStats> = emptyList()
        override suspend fun countDue(ops: Set<FactKey.Op>, nowMs: Long): Int = 0
        override suspend fun upsert(stats: FactStats) = Unit
        override suspend fun applyAnswer(key: FactKey, correct: Boolean, timeMs: Long?, nowMs: Long, errorType: String?) = Unit
        override suspend fun ensureKeys(keys: Set<FactKey>) = Unit
    }
}

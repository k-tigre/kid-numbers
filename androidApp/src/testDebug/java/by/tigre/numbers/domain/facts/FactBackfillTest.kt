package by.tigre.numbers.domain.facts

import by.tigre.numbers.data.facts.FactBackfillPreferences
import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.FactStats
import by.tigre.numbers.entity.GameType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FactBackfillTest {

    private val json: Json = Json

    @Test
    fun runIfNeededWithEmptyHistorySetsFlag() = runBlocking {
        val preferences: FakeFactBackfillPreferences = FakeFactBackfillPreferences()
        val factStore: FakeFactStore = FakeFactStore()
        val backfill: FactBackfill = FactBackfill(
            loadSessions = { emptyList() },
            factStore = factStore,
            preferences = preferences,
            json = json,
        )
        backfill.runIfNeeded()
        assertTrue(preferences.isDone())
        assertEquals(0, factStore.calls.size)
    }

    @Test
    fun runIfNeededSkipsWhenAlreadyDone() = runBlocking {
        val preferences: FakeFactBackfillPreferences = FakeFactBackfillPreferences(done = true)
        val factStore: FakeFactStore = FakeFactStore()
        val backfill: FactBackfill = FactBackfill(
            loadSessions = { error("should not load") },
            factStore = factStore,
            preferences = preferences,
            json = json,
        )
        backfill.runIfNeeded()
        assertEquals(0, factStore.calls.size)
    }

    @Test
    fun backfillSessionsMixedValidInvalidAppliesValidOnly() = runBlocking {
        val preferences: FakeFactBackfillPreferences = FakeFactBackfillPreferences()
        val factStore: FakeFactStore = FakeFactStore()
        val backfill: FactBackfill = FactBackfill(
            loadSessions = { emptyList() },
            factStore = factStore,
            preferences = preferences,
            json = json,
        )
        val validData: String = json.encodeToString(
            GameResult.serializer(),
            sampleGameResult(
                GameResult.Result(
                    isCorrect = true,
                    question = GameOptions.Question.Operation.Additional(a = 2, b = 3),
                    answer = 5,
                    timeMs = 1200L,
                ),
            ),
        )
        backfill.backfillSessions(
            listOf(
                FactBackfill.SessionRow(id = 2L, date = 2000L, data = validData),
                FactBackfill.SessionRow(id = 1L, date = 1000L, data = "{not json"),
                FactBackfill.SessionRow(id = 3L, date = 3000L, data = validData),
            ),
        )
        assertEquals(2, factStore.calls.size)
        assertEquals(2000L, factStore.calls[0].nowMs)
        assertEquals(3000L, factStore.calls[1].nowMs)
    }

    @Test
    fun backfillSessionsEquationOnlyAppliesNothing() = runBlocking {
        val factStore: FakeFactStore = FakeFactStore()
        val backfill: FactBackfill = createBackfill(factStore)
        val data: String = json.encodeToString(
            GameResult.serializer(),
            sampleGameResult(
                GameResult.Result(
                    isCorrect = false,
                    question = GameOptions.Question.Equation.Single(x = 7, title = "x + 2 = 7"),
                    answer = null,
                ),
            ),
        )
        backfill.backfillSessions(listOf(FactBackfill.SessionRow(id = 1L, date = 1000L, data = data)))
        assertEquals(0, factStore.calls.size)
    }

    @Test
    fun backfillSessionsMissingTimeMsAppliesWithNullTime() = runBlocking {
        val factStore: FakeFactStore = FakeFactStore()
        val backfill: FactBackfill = createBackfill(factStore)
        val data: String = json.encodeToString(
            GameResult.serializer(),
            sampleGameResult(
                GameResult.Result(
                    isCorrect = true,
                    question = GameOptions.Question.Operation.Multiplication(first = 3, second = 4),
                    answer = 12,
                ),
            ),
        )
        backfill.backfillSessions(listOf(FactBackfill.SessionRow(id = 1L, date = 5000L, data = data)))
        assertEquals(1, factStore.calls.size)
        assertEquals(null, factStore.calls[0].timeMs)
        assertEquals(FactKey(FactKey.Op.MUL, 3, 4), factStore.calls[0].key)
    }

    private fun createBackfill(factStore: FakeFactStore): FactBackfill {
        return FactBackfill(
            loadSessions = { emptyList() },
            factStore = factStore,
            preferences = FakeFactBackfillPreferences(),
            json = json,
        )
    }

    private fun sampleGameResult(vararg results: GameResult.Result): GameResult {
        return GameResult(
            results = results.toList(),
            time = 60_000L,
            type = GameType.Multiplication,
            difficult = Difficult.Medium,
        )
    }

    private class FakeFactBackfillPreferences(
        private var done: Boolean = false,
    ) : FactBackfillPreferences {
        override fun isDone(): Boolean = done
        override fun markDone() {
            done = true
        }
    }

    private class FakeFactStore : FactStore {
        val calls: MutableList<ApplyAnswerCall> = mutableListOf()
        data class ApplyAnswerCall(
            val key: FactKey,
            val correct: Boolean,
            val timeMs: Long?,
            val nowMs: Long,
        )
        override suspend fun applyAnswer(key: FactKey, correct: Boolean, timeMs: Long?, nowMs: Long, errorType: String?) {
            calls.add(ApplyAnswerCall(key, correct, timeMs, nowMs))
        }
        override suspend fun get(key: FactKey): FactStats? = null
        override suspend fun getByOp(op: FactKey.Op): List<FactStats> = emptyList()
        override suspend fun getByOps(ops: Set<FactKey.Op>): List<FactStats> = emptyList()
        override suspend fun countDue(ops: Set<FactKey.Op>, nowMs: Long): Int = 0
        override suspend fun upsert(stats: FactStats) {}
        override suspend fun ensureKeys(keys: Set<FactKey>) {}
    }
}

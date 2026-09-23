package by.tigre.numbers.data.history

import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.entity.FactStats
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultStoreFactUpdateTest {

    private val nowMs: Long = 1_000_000L

    @Test
    fun applyFactUpdatesAppliesScoredOperationsOnly() = runBlocking {
        val fakeFactStore: FakeFactStore = FakeFactStore()
        val results: List<GameResult.Result> = listOf(
            GameResult.Result(
                isCorrect = true,
                question = GameOptions.Question.Operation.Additional(a = 2, b = 3),
                answer = 5,
                timeMs = 1500L,
            ),
            GameResult.Result(
                isCorrect = false,
                question = GameOptions.Question.Operation.Multiplication(first = 3, second = 4),
                answer = 11,
            ),
            GameResult.Result(
                isCorrect = true,
                question = GameOptions.Question.Operation.Division(second = 4, x = 5),
                answer = 5,
            ),
            GameResult.Result(
                isCorrect = false,
                question = GameOptions.Question.Equation.Single(x = 7, title = "x + 2 = 7"),
                answer = null,
            ),
            GameResult.Result(
                isCorrect = true,
                question = GameOptions.Question.Operation.Additional(a = 1, b = 1),
                answer = 2,
                countsForScore = false,
            ),
            GameResult.Result(
                isCorrect = false,
                question = GameOptions.Question.Operation.Subtraction(b = 5, x = 7),
                answer = null,
            ),
        )
        ResultStore.applyFactUpdates(results, fakeFactStore, nowMs)
        assertEquals(4, fakeFactStore.calls.size)
        assertEquals(
            FakeFactStore.ApplyAnswerCall(FactKey(FactKey.Op.ADD, 2, 3), true, 1500L, nowMs, null),
            fakeFactStore.calls[0],
        )
        assertEquals(
            FakeFactStore.ApplyAnswerCall(FactKey(FactKey.Op.MUL, 3, 4), false, null, nowMs, "OFF_BY_ONE"),
            fakeFactStore.calls[1],
        )
        assertEquals(
            FakeFactStore.ApplyAnswerCall(FactKey(FactKey.Op.DIV, 20, 4), true, null, nowMs, null),
            fakeFactStore.calls[2],
        )
        assertEquals(
            FakeFactStore.ApplyAnswerCall(FactKey(FactKey.Op.SUB, 12, 5), false, null, nowMs, null),
            fakeFactStore.calls[3],
        )
    }

    @Test
    fun applyFactUpdatesClassifiesIncorrectAnswersWithValue() = runBlocking {
        val fakeFactStore: FakeFactStore = FakeFactStore()
        val results: List<GameResult.Result> = listOf(
            GameResult.Result(
                isCorrect = false,
                question = GameOptions.Question.Operation.Additional(a = 20, b = 30),
                answer = 51,
            ),
        )
        ResultStore.applyFactUpdates(results, fakeFactStore, nowMs)
        assertEquals("OFF_BY_ONE", fakeFactStore.calls.single().errorType)
    }

    private class FakeFactStore : FactStore {
        val calls: MutableList<ApplyAnswerCall> = mutableListOf()
        data class ApplyAnswerCall(
            val key: FactKey,
            val correct: Boolean,
            val timeMs: Long?,
            val nowMs: Long,
            val errorType: String?,
        )
        override suspend fun applyAnswer(key: FactKey, correct: Boolean, timeMs: Long?, nowMs: Long, errorType: String?) {
            calls.add(ApplyAnswerCall(key, correct, timeMs, nowMs, errorType))
        }
        override suspend fun get(key: FactKey): FactStats? = null
        override suspend fun getByOp(op: FactKey.Op): List<FactStats> = emptyList()
        override suspend fun getByOps(ops: Set<FactKey.Op>): List<FactStats> = emptyList()
        override suspend fun countDue(ops: Set<FactKey.Op>, nowMs: Long): Int = 0
        override suspend fun upsert(stats: FactStats) {}
        override suspend fun ensureKeys(keys: Set<FactKey>) {}
    }
}

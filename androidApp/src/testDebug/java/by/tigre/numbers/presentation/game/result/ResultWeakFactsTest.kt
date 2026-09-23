package by.tigre.numbers.presentation.game.result

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.ErrorType
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultWeakFactsTest {

    @Test
    fun insightPicksMatchingExamplesNotFirstWrongAnswers() {
        val result: GameResult = smartResult(
            wrong(Operation.Additional(0, 1), given = 6),
            wrong(Operation.Additional(3, 6), given = 1),
            wrong(Operation.Additional(3, 7), given = 2),
            wrong(Operation.Additional(8, 5), given = 14),
            wrong(Operation.Additional(9, 4), given = 14),
            wrong(Operation.Additional(7, 3), given = 11),
        )
        val insight: ErrorType? = computeDominantErrorType(result)
        assertEquals(ErrorType.OFF_BY_ONE, insight)
        assertEquals(
            listOf("5 + 8", "4 + 9", "3 + 7"),
            computeWeakFactLabels(result, preferErrorType = insight),
        )
    }

    @Test
    fun withoutInsightShowsFirstWrongFacts() {
        val result: GameResult = smartResult(
            wrong(Operation.Additional(0, 1), given = 6),
            wrong(Operation.Additional(3, 6), given = 1),
            wrong(Operation.Additional(3, 7), given = 2),
        )
        assertNull(computeDominantErrorType(result))
        assertEquals(
            listOf("0 + 1", "3 + 6", "3 + 7"),
            computeWeakFactLabels(result, preferErrorType = null),
        )
    }

    @Test
    fun dominantErrorTypeWhenSessionSharesOffByOne() {
        val result: GameResult = smartResult(
            wrong(Operation.Additional(5, 5), given = 11),
            wrong(Operation.Additional(4, 4), given = 7),
            wrong(Operation.Additional(6, 6), given = 13),
        )
        assertEquals(ErrorType.OFF_BY_ONE, computeDominantErrorType(result))
        assertEquals(
            listOf("5 + 5", "4 + 4", "6 + 6"),
            computeWeakFactLabels(result, preferErrorType = ErrorType.OFF_BY_ONE),
        )
    }

    @Test
    fun noInsightWhenFewerThanTwoMatchingErrors() {
        val result: GameResult = smartResult(
            wrong(Operation.Additional(5, 5), given = 11),
            wrong(Operation.Additional(0, 1), given = 6),
        )
        assertNull(computeDominantErrorType(result))
    }

    private fun smartResult(vararg items: GameResult.Result): GameResult = GameResult(
        results = items.toList(),
        time = 15_000L,
        type = GameType.Additional,
        difficult = Difficult.Medium,
        isSmartPractice = true,
    )

    private fun wrong(question: Operation, given: Int): GameResult.Result = GameResult.Result(
        isCorrect = false,
        question = question,
        answer = given,
    )
}

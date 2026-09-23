package by.tigre.numbers.entity

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameResultSerializationTest {

    private val json: Json = Json

    @Test
    fun decodeLegacyJsonWithoutTimeMsField() {
        val legacyJson = """
            {
              "results": [
                {
                  "isCorrect": true,
                  "question": {
                    "type": "Multiplication",
                    "first": 3,
                    "second": 4
                  },
                  "answer": 12,
                  "counts_for_score": true
                }
              ],
              "time": 60000,
              "game_type": "Multiplication",
              "difficult": "Medium"
            }
        """.trimIndent()
        val decoded: GameResult = json.decodeFromString(legacyJson)
        assertNull(decoded.results.single().timeMs)
    }

    @Test
    fun roundTripWithoutTimeMsEncodesNull() {
        val original: GameResult = sampleGameResult()
        val encoded: String = json.encodeToString(original)
        assertTrue(encoded.contains("time_ms").not())
        val decoded: GameResult = json.decodeFromString(encoded)
        assertNull(decoded.results.single().timeMs)
    }

    @Test
    fun roundTripWithTimeMsPreservesValue() {
        val original: GameResult = sampleGameResult(
            results = listOf(
                GameResult.Result(
                    isCorrect = true,
                    question = GameOptions.Question.Operation.Multiplication(first = 3, second = 4),
                    answer = 12,
                    timeMs = 1500L,
                )
            )
        )
        val encoded: String = json.encodeToString(original)
        assertTrue(encoded.contains("\"time_ms\":1500"))
        val decoded: GameResult = json.decodeFromString(encoded)
        assertEquals(1500L, decoded.results.single().timeMs)
    }

    @Test
    fun isSmartPracticeRoundTrip() {
        val original: GameResult = sampleGameResult(isSmartPractice = true)
        val encoded: String = json.encodeToString(original)
        assertTrue(encoded.contains("is_smart_practice"))
        val decoded: GameResult = json.decodeFromString(encoded)
        assertTrue(decoded.isSmartPractice)
    }

    @Test
    fun isSmartPracticeDefaultsFalseWhenMissing() {
        val legacyJson: String = """
            {
              "results": [],
              "time": 60000,
              "game_type": "Multiplication",
              "difficult": "Medium"
            }
        """.trimIndent()
        val decoded: GameResult = json.decodeFromString(legacyJson)
        assertFalse(decoded.isSmartPractice)
    }

    private fun sampleGameResult(
        results: List<GameResult.Result> = listOf(
            GameResult.Result(
                isCorrect = true,
                question = GameOptions.Question.Operation.Multiplication(first = 3, second = 4),
                answer = 12,
            )
        ),
        isSmartPractice: Boolean = false,
    ): GameResult = GameResult(
        results = results,
        time = 60_000L,
        type = GameType.Multiplication,
        difficult = Difficult.Medium,
        isSmartPractice = isSmartPractice,
    )
}

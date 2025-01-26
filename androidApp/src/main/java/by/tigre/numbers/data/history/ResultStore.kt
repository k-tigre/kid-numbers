package by.tigre.numbers.data.history

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.HistoryGameResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

interface ResultStore {
    suspend fun save(result: GameResult)
    suspend fun save(result: GameResult, challengeId: String)

    suspend fun load(difficult: List<Difficult>, types: List<GameType>, onlySuccess: Boolean): List<HistoryGameResult>
    suspend fun loadForChallenge(id: String): List<HistoryGameResult>
    suspend fun getDetails(id: Long): GameResult?

    class Impl(
        private val database: DatabaseNumbers,
        scope: CoroutineScope,
        private val analytics: EventAnalytics
    ) : ResultStore {

        private val json = Json

        init {
            scope.launch {
                delay(10_000)
                if (database.historyItemsQueries.getCounts().executeAsOneOrNull() != null) {
                    analytics.trackEvent(Event.Action.Logic.WrongCountInDB)
                }
            }
        }

        override suspend fun save(result: GameResult) {
            saveInternal(result, null)
        }

        override suspend fun save(result: GameResult, challengeId: String) {
            saveInternal(result, challengeId)
        }

        private suspend fun saveInternal(result: GameResult, challengeId: String?) {
            val data = json.encodeToString(GameResult.serializer(), result)
            database.historyQueries.insertHistoryWithData(
                date = System.currentTimeMillis(),
                correctCount = result.correctCount,
                difficult = result.difficult,
                totalCount = result.totalCount,
                duration = result.time,
                gameType = result.type,
                historyData = data,
                challengeId = challengeId
            )
        }

        private val historyResultMapper =
            { id: Long, date: Long, duration: Long, itemDifficult: Difficult, correctCount: Int, totalCount: Int, gameType: GameType?, _: String? ->
                HistoryGameResult(
                    difficult = itemDifficult,
                    correctCount = correctCount,
                    date = date,
                    totalCount = totalCount,
                    duration = duration,
                    gameType = gameType,
                    id = id
                )
            }

        override suspend fun load(difficult: List<Difficult>, types: List<GameType>, onlySuccess: Boolean): List<HistoryGameResult> {
            return if (onlySuccess) {
                database.historyQueries.selectByTypeAndDifficultOnlyCorrect(difficult, types, limit = 10_000, historyResultMapper)
            } else {
                database.historyQueries.selectByTypeAndDifficult(difficult, types, limit = 10_000, historyResultMapper)
            }.executeAsList()
        }

        override suspend fun loadForChallenge(id: String): List<HistoryGameResult> {
            return database.historyQueries.selectByChallenge(challengeId = id, limit = 10_000, historyResultMapper)
                .executeAsList()
        }

        override suspend fun getDetails(id: Long): GameResult? {

            return database.historyItemsQueries.getItem(historyId = id).executeAsOneOrNull()?.let { data ->
                try {
                    json.decodeFromString<GameResult>(data)
                } catch (_: Exception) {
                    analytics.trackEvent(event = Event.Action.Logic.Error("GetHistoryItem"))
                    null
                }
            }
        }
    }
}
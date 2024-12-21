package by.tigre.numbers.data

import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.HistoryGameResult
import kotlinx.coroutines.CoroutineScope

interface ResultStore {
    suspend fun save(result: GameResult)

    suspend fun load(): List<HistoryGameResult>

    class Impl(
        private val database: DatabaseNumbers,
        scope: CoroutineScope
    ) : ResultStore {

        override suspend fun save(result: GameResult) {
            database.historyQueries.transaction {
                database.historyQueries.insertHistory(
                    date = System.currentTimeMillis(), // TODO make it more testable
                    correctCount = result.correctCount,
                    difficult = result.difficult,
                    totalCount = result.totalCount,
                    duration = result.time,
                    gameType = result.type
                )
            }
        }

        override suspend fun load(): List<HistoryGameResult> {

            return database.historyQueries.selectAll(
                limit = 1000000,
                mapper = { _, date: Long, duration: Long, difficult: Difficult, correctCount: Int, totalCount: Int, gameType: GameType? ->
                    HistoryGameResult(
                        difficult = difficult,
                        correctCount = correctCount,
                        date = date,
                        totalCount = totalCount,
                        duration = duration,
                        gameType = gameType
                    )
                }
            ).executeAsList()
        }
    }
}
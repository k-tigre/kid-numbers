package by.tigre.numbers.data

import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.HistoryGameResult
import kotlinx.coroutines.CoroutineScope

interface ResultStore {
    suspend fun save(result: GameResult)

    suspend fun load(difficult: List<Difficult>, types: List<GameType>, onlySuccess: Boolean): List<HistoryGameResult>

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

        override suspend fun load(difficult: List<Difficult>, types: List<GameType>, onlySuccess: Boolean): List<HistoryGameResult> {
            val mapper =
                { _: Long, date: Long, duration: Long, itemDifficult: Difficult, correctCount: Int, totalCount: Int, gameType: GameType? ->
                    HistoryGameResult(
                        difficult = itemDifficult,
                        correctCount = correctCount,
                        date = date,
                        totalCount = totalCount,
                        duration = duration,
                        gameType = gameType
                    )
                }
            return if (onlySuccess) {
                database.historyQueries.selectByTypeAndDifficultOnlyCorrect(difficult, types, limit = 10_000, mapper)
            } else {
                database.historyQueries.selectByTypeAndDifficult(difficult, types, limit = 10_000, mapper)
            }.executeAsList()
        }
    }
}
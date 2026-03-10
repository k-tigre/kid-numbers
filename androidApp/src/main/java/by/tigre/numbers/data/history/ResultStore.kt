package by.tigre.numbers.data.history

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.entity.ChallengeCompleted
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.HistoryGameResult
import by.tigre.numbers.entity.StatisticData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

interface ResultStore {
    suspend fun save(result: GameResult)
    suspend fun save(result: GameResult, challengeId: String)

    suspend fun load(difficult: List<Difficult>, types: List<GameType>, onlySuccess: Boolean, limit: Long = 10_000): List<HistoryGameResult>
    suspend fun loadCompletedChallenges(onlySuccess: Boolean): List<ChallengeCompleted>
    suspend fun loadForChallenge(id: String): List<HistoryGameResult>
    suspend fun getDetails(id: Long): GameResult?
    suspend fun loadStatistic(): StatisticData

    class Impl(
        private val database: DatabaseNumbers,
        scope: CoroutineScope,
        private val analytics: EventAnalytics
    ) : ResultStore {

        private val json = Json

        init {
            scope.launch {
                delay(10_000)
                val result = database.historyItemsQueries.getCounts().executeAsOneOrNull()
                if (result != null && result.count > 1) {
                    analytics.trackEvent(Event.Action.Logic.WrongCountInDB)
                    database.historyItemsQueries.drop(result.historyId)
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

        override suspend fun load(
            difficult: List<Difficult>,
            types: List<GameType>,
            onlySuccess: Boolean,
            limit: Long
        ): List<HistoryGameResult> {
            return if (onlySuccess) {
                database.historyQueries.selectByTypeAndDifficultOnlyCorrect(difficult, types, limit = limit, historyResultMapper)
            } else {
                database.historyQueries.selectByTypeAndDifficult(difficult, types, limit = limit, historyResultMapper)
            }.executeAsList()
        }

        override suspend fun loadCompletedChallenges(onlySuccess: Boolean): List<ChallengeCompleted> {
            return database.challengesQueries.getChallengesCompletedWithFilter(
                isSuccess = if (onlySuccess) listOf(true) else listOf(false, true)
            ).executeAsList()
                .mapNotNull { (id, startDate, endDate, isSuccess, taskCount) ->
                    endDate ?: return@mapNotNull null
                    startDate ?: return@mapNotNull null

                    ChallengeCompleted(
                        id = id,
                        duration = endDate - startDate,
                        startTime = startDate,
                        taskCount = taskCount.toInt(),
                        isSuccess = isSuccess
                    )
                }
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

        override suspend fun loadStatistic(): StatisticData {
            val now = System.currentTimeMillis()
            val since7Days = now - 7L * 86_400_000L
            val since30Days = now - 30L * 86_400_000L

            val total = database.historyQueries.selectStatisticTotal().executeAsOneOrNull()
            val totalCorrect = (total?.correct?.toLong()) ?: 0L
            val totalAll = (total?.total?.toLong()) ?: 0L

            val byType = database.historyQueries.selectStatisticByType().executeAsList()
                .mapNotNull { row ->
                    val gameType = row.gameType ?: return@mapNotNull null
                    gameType to StatisticData.TypeStatistic(
                        correct = row.correct?.toLong() ?: 0L,
                        total = row.total?.toLong() ?: 0L
                    )
                }.toMap()

            val avg7Days = calculatePeriodAverage(since7Days)
            val avg30Days = calculatePeriodAverage(since30Days)
            val avg7DaysByType = calculatePeriodAverageByType(since7Days)
            val avg30DaysByType = calculatePeriodAverageByType(since30Days)

            return StatisticData(
                totalCorrect = totalCorrect,
                totalAll = totalAll,
                byType = byType,
                avg7Days = avg7Days,
                avg30Days = avg30Days,
                avg7DaysByType = avg7DaysByType,
                avg30DaysByType = avg30DaysByType
            )
        }

        private suspend fun calculatePeriodAverage(since: Long): StatisticData.PeriodAverage {
            val stats = database.historyQueries.selectStatisticTotalByPeriod(since).executeAsOneOrNull()
            val daysResult = database.historyQueries.selectDaysCountByPeriod(since).executeAsOneOrNull()
            val days = daysResult?.toLong() ?: 0L
            return if (days > 0L) {
                StatisticData.PeriodAverage(
                    correctPerDay = (stats?.correct?.toFloat() ?: 0f) / days,
                    totalPerDay = (stats?.total?.toFloat() ?: 0f) / days
                )
            } else {
                StatisticData.PeriodAverage(0f, 0f)
            }
        }

        private suspend fun calculatePeriodAverageByType(since: Long): Map<GameType, StatisticData.PeriodAverage> {
            val stats = database.historyQueries.selectStatisticByPeriod(since).executeAsList()
            val days = database.historyQueries.selectDaysCountByTypeAndPeriod(since).executeAsList()
            val daysMap = days.mapNotNull { row ->
                val gameType = row.gameType ?: return@mapNotNull null
                gameType to (row.days?.toLong() ?: 0L)
            }.toMap()

            return stats.mapNotNull { row ->
                val gameType = row.gameType ?: return@mapNotNull null
                val dayCount = daysMap[gameType] ?: 0L
                if (dayCount > 0L) {
                    gameType to StatisticData.PeriodAverage(
                        correctPerDay = (row.correct?.toFloat() ?: 0f) / dayCount,
                        totalPerDay = (row.total?.toFloat() ?: 0f) / dayCount
                    )
                } else {
                    gameType to StatisticData.PeriodAverage(0f, 0f)
                }
            }.toMap()
        }
    }
}
package by.tigre.numbers.entity

data class StatisticData(
    val totalCorrect: Long,
    val totalAll: Long,
    val byType: Map<GameType, TypeStatistic>,
    val avg7Days: PeriodAverage,
    val avg30Days: PeriodAverage,
    val avg7DaysByType: Map<GameType, PeriodAverage>,
    val avg30DaysByType: Map<GameType, PeriodAverage>,
) {
    val correctPercent: Float by lazy { if (totalAll > 0) totalCorrect.toFloat() / totalAll * 100f else 0f }

    data class TypeStatistic(val correct: Long, val total: Long) {
        val correctPercent: Float by lazy { if (total > 0) correct.toFloat() / total * 100f else 0f }
    }

    data class PeriodAverage(val correctPerDay: Float, val totalPerDay: Float)
}

package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.LeaderboardSpeedComparison
import by.tigre.numbers.entity.LeaderboardSpeedEntry

object LeaderboardSpeedComparisonCalculator {

    fun compare(
        elapsedSeconds: Long,
        mistakes: Int,
        bestEntry: LeaderboardSpeedEntry?,
    ): LeaderboardSpeedComparison {
        if (bestEntry == null) return LeaderboardSpeedComparison.FirstOnBoard
        val deltaSeconds: Double = (elapsedSeconds - bestEntry.bestTimeSeconds).toDouble()
        return when {
            deltaSeconds < 0 -> LeaderboardSpeedComparison.FasterThanBest(-deltaSeconds)
            deltaSeconds > 0 -> LeaderboardSpeedComparison.BehindBest(deltaSeconds)
            mistakes <= bestEntry.mistakes -> LeaderboardSpeedComparison.MatchedBest
            else -> LeaderboardSpeedComparison.BehindBest(0.0)
        }
    }
}

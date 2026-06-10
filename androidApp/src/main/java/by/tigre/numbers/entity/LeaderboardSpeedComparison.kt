package by.tigre.numbers.entity

sealed interface LeaderboardSpeedComparison {
    data object FirstOnBoard : LeaderboardSpeedComparison
    data class FasterThanBest(val seconds: Double) : LeaderboardSpeedComparison
    data class BehindBest(val seconds: Double) : LeaderboardSpeedComparison
    data object MatchedBest : LeaderboardSpeedComparison
}

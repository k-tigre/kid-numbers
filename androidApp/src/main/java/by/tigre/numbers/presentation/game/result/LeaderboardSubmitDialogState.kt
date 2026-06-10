package by.tigre.numbers.presentation.game.result

import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.LeaderboardSpeedComparison

data class LeaderboardSubmitDialogState(
    val gameScore: Int,
    val defaultNickname: String,
    val settings: GameSettings,
    val boardKey: String,
    val elapsedSeconds: Long,
    val mistakes: Int,
    val speedComparison: LeaderboardSpeedComparison? = null,
    val submitError: SubmitError? = null,
    val submitted: Boolean = false,
    val submitSkipped: Boolean = false,
) {
    enum class SubmitError {
        EmptyNickname,
        Generic,
    }
}

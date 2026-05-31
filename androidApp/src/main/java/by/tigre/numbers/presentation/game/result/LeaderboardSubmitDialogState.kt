package by.tigre.numbers.presentation.game.result

import by.tigre.numbers.entity.Difficult

data class LeaderboardSubmitDialogState(
    val gameScore: Int,
    val defaultNickname: String,
    val difficult: Difficult,
    val elapsedSeconds: Long,
    val mistakes: Int,
    val submitError: SubmitError? = null,
    val submitted: Boolean = false,
    val submitSkipped: Boolean = false,
) {
    enum class SubmitError {
        EmptyNickname,
        Generic,
    }
}

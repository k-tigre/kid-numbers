package by.tigre.numbers.presentation.game.result

import by.tigre.numbers.data.leaderboard.LeaderboardBoardKey
import by.tigre.numbers.data.leaderboard.LeaderboardRepository
import by.tigre.numbers.data.leaderboard.LeaderboardSpeedComparisonCalculator
import by.tigre.numbers.data.remoteconfig.FeatureFlags
import by.tigre.numbers.data.storage.LeaderboardPreferences
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.LeaderboardSpeedSubmit
import by.tigre.numbers.entity.LeaderboardSubmitRequest
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ResultComponent {

    val results: StateFlow<GameResult>
    val leaderboardDialog: StateFlow<LeaderboardSubmitDialogState?>

    fun onClose()
    fun onSubmitScore(nickname: String)
    fun onSkipLeaderboardSubmit()

    class Impl(
        context: BaseComponentContext,
        result: GameResult,
        private val featureFlags: FeatureFlags,
        private val leaderboardRepository: LeaderboardRepository,
        private val leaderboardPreferences: LeaderboardPreferences,
        private val onFinish: () -> Unit,
    ) : ResultComponent, BaseComponentContext by context {

        override val results: StateFlow<GameResult> = MutableStateFlow(result)
        private val _leaderboardDialog = MutableStateFlow<LeaderboardSubmitDialogState?>(null)
        override val leaderboardDialog: StateFlow<LeaderboardSubmitDialogState?> = _leaderboardDialog.asStateFlow()

        init {
            val settings: GameSettings? = result.settings
            if (
                settings != null &&
                featureFlags.isLeaderboardEnabled.value &&
                result.correctCount == result.totalCount &&
                result.totalCount > 0
            ) {
                val elapsedSeconds: Long = result.time / 1000L
                val mistakes: Int = result.inCorrectCount
                val boardKey: String = LeaderboardBoardKey.from(settings)
                leaderboardPreferences.saveLastBoardKey(boardKey)
                leaderboardPreferences.saveLastBoardSettings(settings)
                _leaderboardDialog.value = LeaderboardSubmitDialogState(
                    gameScore = featureFlags.calculateLeaderboardRating(
                        settings = settings,
                        elapsedSeconds = elapsedSeconds,
                        hintsUsed = 0,
                        mistakes = mistakes,
                    ),
                    defaultNickname = leaderboardPreferences.loadNickname(default = DEFAULT_NICKNAME),
                    settings = settings,
                    boardKey = boardKey,
                    elapsedSeconds = elapsedSeconds,
                    mistakes = mistakes,
                )
                launch {
                    leaderboardRepository.fetchBestSpeedEntry(boardKey)
                        .onSuccess { bestEntry ->
                            val dialog: LeaderboardSubmitDialogState = _leaderboardDialog.value ?: return@onSuccess
                            _leaderboardDialog.value = dialog.copy(
                                speedComparison = LeaderboardSpeedComparisonCalculator.compare(
                                    elapsedSeconds = elapsedSeconds,
                                    mistakes = mistakes,
                                    bestEntry = bestEntry,
                                ),
                            )
                        }
                }
            }
        }

        override fun onClose() = onFinish()

        override fun onSubmitScore(nickname: String) {
            val dialog: LeaderboardSubmitDialogState = _leaderboardDialog.value ?: return
            if (dialog.submitted) return
            val trimmed: String = nickname.trim()
            if (trimmed.isEmpty()) {
                _leaderboardDialog.value = dialog.copy(submitError = LeaderboardSubmitDialogState.SubmitError.EmptyNickname)
                return
            }
            _leaderboardDialog.value = dialog.copy(submitted = true, submitError = null)
            if (leaderboardPreferences.loadNickname(default = DEFAULT_NICKNAME) != trimmed) {
                leaderboardPreferences.saveNickname(trimmed)
            }
            val userId: String = leaderboardPreferences.getOrCreateUserId()
            val timestampMillis: Long = System.currentTimeMillis()
            val speedRequest = LeaderboardSpeedSubmit(
                userId = userId,
                nickname = trimmed,
                boardKey = dialog.boardKey,
                gameType = results.value.type,
                difficult = dialog.settings.difficult,
                solveTimeSeconds = dialog.elapsedSeconds,
                mistakes = dialog.mistakes,
                taskCount = results.value.totalCount,
                timestampMillis = timestampMillis,
            )
            val totalRequest = LeaderboardSubmitRequest(
                userId = userId,
                nickname = trimmed,
                gameScore = dialog.gameScore,
                solveTimeSeconds = dialog.elapsedSeconds,
                hintsUsed = 0,
                mistakes = dialog.mistakes,
                timeCapSeconds = featureFlags.timeCapSeconds(dialog.settings),
                timestampMillis = timestampMillis,
            )
            launch {
                val speedResult = leaderboardRepository.submitSpeedRecord(speedRequest)
                val totalResult = leaderboardRepository.addTotalScore(totalRequest)
                if (speedResult.isFailure || totalResult.isFailure) {
                    _leaderboardDialog.value = dialog.copy(
                        submitted = false,
                        submitError = LeaderboardSubmitDialogState.SubmitError.Generic,
                    )
                }
            }
        }

        override fun onSkipLeaderboardSubmit() {
            val dialog: LeaderboardSubmitDialogState = _leaderboardDialog.value ?: return
            _leaderboardDialog.value = dialog.copy(submitSkipped = true)
        }

        private companion object {
            const val DEFAULT_NICKNAME: String = "Player"
        }
    }
}

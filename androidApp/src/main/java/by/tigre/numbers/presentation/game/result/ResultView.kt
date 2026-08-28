package by.tigre.numbers.presentation.game.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.entity.LeaderboardSpeedComparison
import by.tigre.numbers.presentation.leaderboard.toLeaderboardBoardLabel
import by.tigre.tools.tools.platform.compose.ScreenComposableView

class ResultView(
    private val component: ResultComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_game_result_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(
            vector = Icons.Default.Close,
            contentDescription = { stringResource(R.string.content_desc_close) },
            action = component::onClose,
        ),
    ),
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val result by component.results.collectAsState()
        val dialogState by component.leaderboardDialog.collectAsState()
        DrawLeaderboardDialog(dialogState)
        ResultScreenContent(result = result, innerPadding = innerPadding)
    }

    @Composable
    private fun DrawLeaderboardDialog(dialogState: LeaderboardSubmitDialogState?) {
        if (dialogState == null || dialogState.submitSkipped || dialogState.submitted) return
        var nickname by remember(dialogState.defaultNickname) { mutableStateOf(dialogState.defaultNickname) }
        AlertDialog(
            onDismissRequest = component::onSkipLeaderboardSubmit,
            title = { Text(stringResource(R.string.screen_leaderboard_submit_nickname_title)) },
            text = {
                Column {
                    Text(dialogState.settings.toLeaderboardBoardLabel())
                    Text(stringResource(R.string.screen_leaderboard_submit_rating, dialogState.gameScore))
                    dialogState.speedComparison?.let { comparison ->
                        Text(
                            text = comparison.toMessage(),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(24) },
                        label = { Text(stringResource(R.string.screen_leaderboard_submit_nickname)) },
                        singleLine = true,
                    )
                    dialogState.submitError?.let { error ->
                        val message = when (error) {
                            LeaderboardSubmitDialogState.SubmitError.EmptyNickname ->
                                stringResource(R.string.screen_leaderboard_submit_error_empty_nickname)
                            LeaderboardSubmitDialogState.SubmitError.Generic ->
                                stringResource(R.string.screen_leaderboard_submit_error_generic)
                        }
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { component.onSubmitScore(nickname) }) {
                    Text(stringResource(R.string.screen_leaderboard_submit_send))
                }
            },
            dismissButton = {
                TextButton(onClick = component::onSkipLeaderboardSubmit) {
                    Text(stringResource(R.string.screen_leaderboard_submit_skip))
                }
            },
        )
    }

    @Composable
    private fun LeaderboardSpeedComparison.toMessage(): String = when (this) {
        LeaderboardSpeedComparison.FirstOnBoard -> stringResource(R.string.screen_leaderboard_submit_speed_first)
        is LeaderboardSpeedComparison.FasterThanBest -> stringResource(R.string.screen_leaderboard_submit_speed_faster, seconds)
        is LeaderboardSpeedComparison.BehindBest -> stringResource(R.string.screen_leaderboard_submit_speed_behind, seconds)
        LeaderboardSpeedComparison.MatchedBest -> stringResource(R.string.screen_leaderboard_submit_speed_matched)
    }
}

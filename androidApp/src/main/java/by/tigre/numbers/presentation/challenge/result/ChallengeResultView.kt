package by.tigre.numbers.presentation.challenge.result

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.result.ResultView
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.LocalGameColorsPalette
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize
import com.arkivanov.decompose.extensions.compose.subscribeAsState

class ChallengeResultView(
    private val component: ChallengeResultComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_challenge_result_title) },
        navigationIcon = null
    )
) {

    @Composable
    override fun Draw(modifier: Modifier) {
        val details by component.details.subscribeAsState()
        AnimatedContent(
            modifier = modifier,
            targetState = details.child?.instance,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
        ) { item ->
            if (item == null) {
                super.Draw(modifier)
            } else {
                ResultView(item).Draw(modifier)
            }
        }
    }

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        when (val challenge = component.results.collectAsState().value) {
            is ChallengeResultComponent.ScreenState.History -> {
                Column(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        item {
                            Column(Modifier.fillMaxWidth()) {

                                val color = if (challenge.isSuccess) {
                                    LocalGameColorsPalette.current.gameSuccess
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalGameColorsPalette.current.gameFailed
                                    MaterialTheme.colorScheme.error
                                }

                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 32.dp),
                                    color = color,
                                    text = stringResource(
                                        R.string.screen_challenge_result_duration,
                                        TIME_FORMAT.format(challenge.endDate - challenge.startDate)
                                    )
                                )

                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 32.dp),
                                    color = color,
                                    text = stringResource(
                                        R.string.screen_challenge_result_total_task,
                                        challenge.items.size
                                    ),
                                )

                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 32.dp),
                                    color = color,
                                    text = stringResource(
                                        R.string.screen_challenge_result_correct_task,
                                        challenge.items.count { it.totalCount == it.correctCount }
                                    ),
                                )
                            }
                        }

                        items(items = challenge.items) {
                            DrawItem(it)
                        }
                    }

                    Button(
                        onClick = { component.onCloseClicked() },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.button_close))
                    }
                }
            }

            ChallengeResultComponent.ScreenState.Loading -> {
                ProgressIndicator(size = ProgressIndicatorSize.LARGE)
            }
        }
    }

    @Composable
    private fun DrawItem(result: ChallengeResultComponent.HistoryItem) {
        val colors = if (result.isCorrect) {
            LocalGameColorsPalette.current.gameSuccess
        } else {
            LocalGameColorsPalette.current.gameFailed
        }
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors().copy(
                containerColor = colors.colorContainer
            ),
            onClick = { component.onItemClicked(result) }
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.screen_game_result_duration, TIME_FORMAT.format(result.duration)),
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.screen_game_result_total_questions, result.totalCount)
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.screen_game_result_total_correct_answers, result.correctCount),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.screen_game_result_total_wrong_answers, result.totalCount - result.correctCount),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

package by.tigre.numbers.presentation.challenge.result

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import by.tigre.numbers.presentation.game.result.ResultStatItem
import by.tigre.numbers.presentation.game.result.ResultSummaryCard
import by.tigre.numbers.presentation.game.result.ResultView
import by.tigre.numbers.presentation.game.result.resolveResultScoreBand
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.Dimens
import by.tigre.tools.tools.platform.compose.LocalGameColorsPalette
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize
import by.tigre.tools.tools.platform.compose.view.SectionHeader
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
                ResultView(item).Draw(modifier.safeDrawingPadding())
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
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.md),
                            ) {
                                val perfectTasks: Int = challenge.items.count { it.correctCount == it.totalCount }
                                val accentColor = if (challenge.isSuccess) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                                ResultSummaryCard(
                                    band = resolveResultScoreBand(
                                        correctCount = perfectTasks,
                                        totalCount = challenge.items.size,
                                    ),
                                    stats = listOf(
                                        ResultStatItem(
                                            label = stringResource(R.string.result_stat_time),
                                            value = TIME_FORMAT.format(challenge.endDate - challenge.startDate),
                                            valueColor = accentColor,
                                        ),
                                        ResultStatItem(
                                            label = stringResource(R.string.result_stat_correct),
                                            value = perfectTasks.toString(),
                                            valueColor = MaterialTheme.colorScheme.primary,
                                        ),
                                        ResultStatItem(
                                            label = stringResource(R.string.result_stat_wrong),
                                            value = (challenge.items.size - perfectTasks).toString(),
                                            valueColor = MaterialTheme.colorScheme.error,
                                        ),
                                    ),
                                )
                                SectionHeader(title = stringResource(R.string.screen_challenge_result_total_task, challenge.items.size))
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
        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = Dimens.md, vertical = Dimens.sm)
                .fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = colors.colorContainer,
            ),
            onClick = { component.onItemClicked(result) },
        ) {
            Column(modifier = Modifier.padding(Dimens.md)) {
                Text(
                    text = stringResource(R.string.screen_game_result_duration, TIME_FORMAT.format(result.duration)),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onColorContainer,
                )
                Text(
                    modifier = Modifier.padding(top = Dimens.xs),
                    text = stringResource(R.string.screen_game_result_total_questions, result.totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onColorContainer.copy(alpha = 0.85f),
                )
                Text(
                    modifier = Modifier.padding(top = Dimens.xs),
                    text = stringResource(R.string.screen_game_result_total_correct_answers, result.correctCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (result.totalCount - result.correctCount > 0) {
                    Text(
                        modifier = Modifier.padding(top = Dimens.xs),
                        text = stringResource(
                            R.string.screen_game_result_total_wrong_answers,
                            result.totalCount - result.correctCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

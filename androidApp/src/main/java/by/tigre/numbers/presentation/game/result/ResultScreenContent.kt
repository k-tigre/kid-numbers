package by.tigre.numbers.presentation.game.result

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.settings.gameSettingsConfirmButtonColors
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.Dimens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultScreenContent(
    result: GameResult,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    weakFactLabels: List<String> = emptyList(),
    errorInsight: String? = null,
    onDoneClicked: () -> Unit,
) {
    var answersExpanded by rememberSaveable(result.inCorrectCount) {
        mutableStateOf(result.inCorrectCount > 0)
    }
    LazyVerticalGrid(
        modifier = modifier
            .padding(innerPadding)
            .fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 168.dp),
        contentPadding = PaddingValues(Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ResultSummaryCard(
                band = result.scoreBand(),
                stats = listOf(
                    ResultStatItem(
                        label = stringResource(R.string.result_stat_time),
                        value = TIME_FORMAT.format(result.time),
                    ),
                    ResultStatItem(
                        label = stringResource(R.string.result_stat_correct),
                        value = result.correctCount.toString(),
                        valueColor = MaterialTheme.colorScheme.primary,
                    ),
                    ResultStatItem(
                        label = stringResource(R.string.result_stat_wrong),
                        value = result.inCorrectCount.toString(),
                        valueColor = MaterialTheme.colorScheme.error,
                    ),
                ),
            )
        }
        if (result.isSmartPractice) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ResultSmartNoRankBadge()
            }
            if (weakFactLabels.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ResultWeakFactsCard(labels = weakFactLabels, errorInsight = errorInsight)
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.sm, bottom = Dimens.sm),
                onClick = onDoneClicked,
                colors = gameSettingsConfirmButtonColors(),
            ) {
                Text(text = stringResource(R.string.result_done_button))
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            ResultAnswersHeader(
                expanded = answersExpanded,
                onClick = { answersExpanded = answersExpanded.not() },
            )
        }
        if (answersExpanded) {
            itemsIndexed(items = result.results) { _, item ->
                ResultQuestionCard(result = item)
            }
        }
    }
}

@Composable
private fun ResultAnswersHeader(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                role = Role.Button,
            )
            .padding(top = Dimens.sm, bottom = Dimens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.result_section_answers),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AnimatedContent(targetState = expanded, label = "answers_expand") { isExpanded ->
            Icon(
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(
                    if (isExpanded) R.string.result_answers_collapse else R.string.result_answers_expand
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showSystemUi = true)
@Composable
private fun ResultScreenContentPreview() {
    AppTheme {
        ResultScreenContent(
            result = GameResult(
                results = listOf(
                    GameResult.Result(isCorrect = true, question = Operation.Multiplication(3, 4), answer = 12),
                    GameResult.Result(isCorrect = false, question = Operation.Additional(2, 5), answer = 6),
                ),
                time = 45_000L,
                difficult = Difficult.Easy,
                type = GameType.Multiplication,
            ),
            innerPadding = PaddingValues(),
            onDoneClicked = {},
        )
    }
}

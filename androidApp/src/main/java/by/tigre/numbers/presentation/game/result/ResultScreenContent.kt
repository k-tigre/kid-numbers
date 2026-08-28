package by.tigre.numbers.presentation.game.result

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.Dimens
import by.tigre.tools.tools.platform.compose.view.SectionHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultScreenContent(
    result: GameResult,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
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
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = stringResource(R.string.result_section_answers))
        }
        itemsIndexed(items = result.results) { _, item ->
            ResultQuestionCard(result = item)
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
        )
    }
}

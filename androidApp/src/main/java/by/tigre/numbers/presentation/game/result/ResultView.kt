package by.tigre.numbers.presentation.game.result

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ComposableView
import by.tigre.tools.tools.platform.compose.LocalGameColorsPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ResultView(
    private val component: ResultComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(modifier.fillMaxSize()) {
            val result by component.results.collectAsState()

            IconButton(onClick = component::onClose, modifier = Modifier.align(Alignment.End)) {
                Icon(painter = painterResource(id = R.drawable.baseline_close_24), contentDescription = "")
            }

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = stringResource(R.string.screen_game_result_duration, TIME_FORMAT.format(result.time)),
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
                text = stringResource(R.string.screen_game_result_total_wrong_answers, result.inCorrectCount),
                color = MaterialTheme.colorScheme.error,
            )

            LazyVerticalGrid(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                result.results.forEach {
                    item {
                        DrawItem(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawItem(result: GameResult.Result) {
        val colors = if (result.isCorrect) {
            LocalGameColorsPalette.current.gameSuccess
        } else {
            LocalGameColorsPalette.current.gameFailed
        }
        Card(
            modifier = Modifier,
            colors = CardDefaults.cardColors().copy(
                containerColor = colors.colorContainer
            )
        ) {
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = "${result.question.title} = ${if (result.answer != null) result.question.result else "***"}",
                color = colors.onColorContainer
            )

            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                text = stringResource(R.string.screen_game_result_item_user_answer, result.answer ?: ""),
                color = colors.onColorContainer
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, device = Devices.NEXUS_10)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val component = object : ResultComponent {
        override val results: StateFlow<GameResult> = MutableStateFlow(
            GameResult(
                results = (1..4).map {
                    listOf(
                        GameResult.Result(
                            isCorrect = it % 2 == 0,
                            question = GameOptions.Question.Multiplication(it, 2),
                            answer = 19434
                        ),
                        GameResult.Result(
                            isCorrect = it % 2 == 0,
                            question = GameOptions.Question.Multiplication(it, 2),
                            answer = null
                        ),
                        GameResult.Result(
                            isCorrect = it % 2 == 0,
                            question = GameOptions.Question.Additional(it, 2),
                            answer = 109
                        ),
                        GameResult.Result(
                            isCorrect = it % 2 == 0,
                            question = GameOptions.Question.Additional(it, 2),
                            answer = null
                        )
                    )
                }.flatten(),
                time = 23823,
                difficult = Difficult.Easy,
                type = GameType.Multiplication
            )
        )

        override fun onClose() {
            TODO("Not yet implemented")
        }
    }

    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            ResultView(
                component = component,
            ).Draw(Modifier)
        }
    }
}

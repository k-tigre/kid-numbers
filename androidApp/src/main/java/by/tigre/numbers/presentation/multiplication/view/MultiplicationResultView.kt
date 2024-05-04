package by.tigre.numbers.presentation.multiplication.view

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.multiplication.GameResult
import by.tigre.numbers.presentation.multiplication.component.MultiplicationResultComponent
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ComposableView
import by.tigre.tools.tools.platform.compose.LocalCustomColorsPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MultiplicationResultView(
    private val component: MultiplicationResultComponent,
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
                text = "Время выполнения: ${result.time}",
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = "Результат ответов на ${result.totalCount} вопросов"
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = "Всего правильных ответов: ${result.correctCount}",
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                text = "Всего НЕ правильных ответов: ${result.inCorrectCount}",
                color = MaterialTheme.colorScheme.error,
            )

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .align(Alignment.CenterHorizontally),
                columns = GridCells.Adaptive(120.dp),
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
            LocalCustomColorsPalette.current.customColor1
        } else {
            LocalCustomColorsPalette.current.customColor2
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
                text = "${result.question.first} * ${result.question.second} = ${result.question.correctAnswer ?: "***"}",
                color = colors.onColorContainer
            )

            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                text = "Твой ответ: ${result.question.answer ?: ""}",
                color = colors.onColorContainer
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val component = object : MultiplicationResultComponent {
        override val results: StateFlow<GameResult> = MutableStateFlow(
            GameResult(
                results = (1..20).map {
                    listOf(
                        GameResult.Result(
                            isCorrect = it % 2 == 0,
                            question = GameResult.Question(it, 2, 2, 3)
                        )
                    )
                }.flatten(),
                time = "19:10"
            )
        )

        override fun onClose() {
            TODO("Not yet implemented")
        }
    }

    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            MultiplicationResultView(
                component = component,
            ).Draw(Modifier)
        }
    }
}
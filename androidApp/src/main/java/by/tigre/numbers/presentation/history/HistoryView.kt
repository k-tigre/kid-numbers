package by.tigre.numbers.presentation.history

import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.HistoryGameResult
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class HistoryView(
    private val component: HistoryComponent,
) : ScreenComposableView(
    ToolbarConfig.Default(
        title = { stringResource(R.string.screen_history_title) },
        onBackClicked = component::onCloseClicked
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val results = component.results.collectAsState()
        when (val state = results.value) {
            is HistoryComponent.ScreenState.Loading -> {
                // TODO add loading indicator
            }

            is HistoryComponent.ScreenState.Empty -> {
                Box(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        text = stringResource(R.string.screen_history_empty),
                        textAlign = TextAlign.Center
                    )
                }
            }

            is HistoryComponent.ScreenState.History -> {
                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    columns = GridCells.Adaptive(160.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.items.forEach {
                        item(key = it.date) {
                            Column {
                                Text(
                                    text = stringResource(
                                        R.string.screen_history_item_date,
                                        SimpleDateFormat.getDateTimeInstance().format(it.date)
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.screen_history_item_duration,
                                        TIME_FORMAT.format(it.duration)
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.screen_history_item_total_questions,
                                        it.totalCount
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.screen_history_item_correct_answers,
                                        it.correctCount
                                    )
                                )
                                Text(
                                    text = stringResource(
                                        R.string.screen_history_item_difficulty,
                                        it.difficult.toLabel()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showSystemUi = true, device = Devices.NEXUS_10)
@Composable
private fun Preview() {
    val component = object : HistoryComponent {
        override val results: StateFlow<HistoryComponent.ScreenState> = MutableStateFlow(
            HistoryComponent.ScreenState.History(
                items = (1..20).map {
                    HistoryGameResult(
                        date = Random.nextLong(),
                        duration = 12128217,
                        difficult = Difficult.Hard,
                        correctCount = it * 10,
                        totalCount = 12
                    )
                }
            )
        )

        override fun onCloseClicked() {
            TODO("Not yet implemented")
        }

    }

    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            HistoryView(
                component = component,
            ).Draw(Modifier)
        }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEmpty() {
    val component = object : HistoryComponent {
        override val results: StateFlow<HistoryComponent.ScreenState> = MutableStateFlow(
            HistoryComponent.ScreenState.Empty
        )

        override fun onCloseClicked() {
            TODO("Not yet implemented")
        }

    }

    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            HistoryView(
                component = component,
            ).Draw(Modifier)
        }
    }
}
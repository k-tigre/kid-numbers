package by.tigre.numbers.presentation.statistic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import by.tigre.numbers.R
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.StatisticData
import by.tigre.numbers.entity.StatisticData.TypeStatistic
import by.tigre.numbers.presentation.game.GameView
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import java.util.Locale
import by.tigre.tools.tools.platform.compose.view.EmptyScreen
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StatisticView(
    private val component: StatisticComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_statistic_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onCloseClicked)
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val state = component.screenState.collectAsState()

        AnimatedContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            targetState = state.value,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            contentKey = { it::class.simpleName }
        ) { screenState ->
            when (screenState) {
                is StatisticComponent.ScreenState.Loading -> DrawLoading()
                is StatisticComponent.ScreenState.Empty -> DrawEmpty()
                is StatisticComponent.ScreenState.Data -> DrawStatistic(screenState)
            }
        }
    }

    @Composable
    private fun DrawLoading() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProgressIndicator(size = ProgressIndicatorSize.LARGE)
        }
    }

    @Composable
    private fun DrawEmpty() {
        EmptyScreen(
            message = stringResource(R.string.screen_statistic_empty),
        )
    }

    @Composable
    private fun DrawStatistic(data: StatisticComponent.ScreenState.Data) {
        val statistic = data.statistic

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DrawStatisticCard(
                    title = stringResource(R.string.screen_statistic_total),
                    totalCorrect = statistic.totalCorrect,
                    totalAll = statistic.totalAll,
                    correctPercent = statistic.correctPercent,
                    avg7Days = statistic.avg7Days,
                    avg30Days = statistic.avg30Days
                )
            }

            data.gameTypes.forEach { gameType ->
                item {
                    val typeStats = statistic.byType[gameType]
                    DrawStatisticCard(
                        title = gameType.toLabel(),
                        totalCorrect = typeStats?.correct ?: 0L,
                        totalAll = typeStats?.total ?: 0L,
                        correctPercent = typeStats?.correctPercent ?: 0f,
                        avg7Days = statistic.avg7DaysByType[gameType]
                            ?: StatisticData.PeriodAverage(0f, 0f),
                        avg30Days = statistic.avg30DaysByType[gameType]
                            ?: StatisticData.PeriodAverage(0f, 0f)
                    )
                }
            }
        }
    }

    @Composable
    private fun DrawStatisticCard(
        title: String,
        totalCorrect: Long,
        totalAll: Long,
        correctPercent: Float,
        avg7Days: StatisticData.PeriodAverage,
        avg30Days: StatisticData.PeriodAverage
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                HorizontalDivider()

                DrawStatRow(
                    label = stringResource(R.string.screen_statistic_avg_7_days),
                    value = stringResource(
                        R.string.screen_statistic_avg_value,
                        formatAvg(avg7Days.correctPerDay),
                        formatAvg(avg7Days.totalPerDay)
                    )
                )

                DrawStatRow(
                    label = stringResource(R.string.screen_statistic_avg_30_days),
                    value = stringResource(
                        R.string.screen_statistic_avg_value,
                        formatAvg(avg30Days.correctPerDay),
                        formatAvg(avg30Days.totalPerDay)
                    )
                )

                HorizontalDivider()

                DrawStatRow(
                    label = stringResource(R.string.screen_statistic_total_solved),
                    value = totalAll.toString()
                )

                DrawStatRow(
                    label = stringResource(R.string.screen_statistic_correct_percent),
                    value = stringResource(
                        R.string.screen_statistic_percent_value,
                        totalCorrect.toInt(),
                        formatPercent(correctPercent)
                    )
                )
            }
        }
    }

    @Composable
    private fun DrawStatRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    private fun formatAvg(value: Float): String {
        return if (value == 0f) "0" else String.format(Locale.US, "%.1f", value)
    }

    private fun formatPercent(value: Float): String {
        return if (value == 0f) "0" else String.format(Locale.US, "%.0f", value)
    }
}

@Preview
@Composable
private fun PreviewDrawStatisticCard() {
    val component = object : StatisticComponent {
        override val screenState: StateFlow<StatisticComponent.ScreenState> = MutableStateFlow(
            StatisticComponent.ScreenState.Data(
                statistic = StatisticData(
                    totalCorrect = 24,
                    totalAll = 87,
                    avg30Days = StatisticData.PeriodAverage(19f, 29f),
                    byType = mapOf(GameType.Multiplication to TypeStatistic(20, 29)),
                    avg7Days = StatisticData.PeriodAverage(19f, 29f),
                    avg7DaysByType = mapOf(GameType.Multiplication to StatisticData.PeriodAverage(19f, 29f)),
                    avg30DaysByType = mapOf(GameType.Multiplication to StatisticData.PeriodAverage(19f, 29f)),
                ),
                gameTypes = listOf(GameType.Multiplication)
            )
        )

        override fun onCloseClicked() = Unit

    }
    AppTheme {
        StatisticView(
            component = component,
        ).Draw(Modifier)
    }
}

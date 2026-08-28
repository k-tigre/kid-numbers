package by.tigre.numbers.presentation.game.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import by.tigre.tools.tools.platform.compose.AppShapes
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.Dimens

data class ResultStatItem(
    val label: String,
    val value: String,
    val valueColor: Color = Color.Unspecified,
)

@Composable
fun ResultSummaryCard(
    band: ResultScoreBand,
    stats: List<ResultStatItem>,
    modifier: Modifier = Modifier,
) {
    val heroContainerColor: Color = when (band) {
        ResultScoreBand.Perfect -> MaterialTheme.colorScheme.tertiaryContainer
        ResultScoreBand.Good -> MaterialTheme.colorScheme.secondaryContainer
        ResultScoreBand.Retry -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val heroContentColor: Color = when (band) {
        ResultScoreBand.Perfect -> MaterialTheme.colorScheme.onTertiaryContainer
        ResultScoreBand.Good -> MaterialTheme.colorScheme.onSecondaryContainer
        ResultScoreBand.Retry -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroContainerColor)
                    .padding(vertical = Dimens.lg, horizontal = Dimens.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ResultHeroHeadline(
                    band = band,
                    color = heroContentColor,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            if (stats.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.md),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    stats.forEachIndexed { index, stat ->
                        if (index > 0) {
                            VerticalDivider(
                                modifier = Modifier.padding(vertical = Dimens.xs),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        ResultStatColumn(stat = stat)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultStatColumn(
    stat: ResultStatItem,
    modifier: Modifier = Modifier,
) {
    val valueColor: Color = if (stat.valueColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        stat.valueColor
    }
    Column(
        modifier = modifier.padding(horizontal = Dimens.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stat.value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultSummaryCardPreview() {
    AppTheme {
        ResultSummaryCard(
            band = ResultScoreBand.Perfect,
            stats = listOf(
                ResultStatItem(label = "Time", value = "01:23"),
                ResultStatItem(label = "Correct", value = "10", valueColor = Color(0xFF2E7D32)),
                ResultStatItem(label = "Wrong", value = "0", valueColor = Color(0xFFC62828)),
            ),
            modifier = Modifier.padding(Dimens.md),
        )
    }
}

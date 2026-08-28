package by.tigre.numbers.presentation.game.result

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import by.tigre.numbers.R
import by.tigre.numbers.entity.GameResult

enum class ResultScoreBand {
    Perfect,
    Good,
    Retry,
}

fun GameResult.scoreBand(): ResultScoreBand = resolveResultScoreBand(correctCount = correctCount, totalCount = totalCount)

fun resolveResultScoreBand(correctCount: Int, totalCount: Int): ResultScoreBand {
    if (totalCount <= 0) return ResultScoreBand.Retry
    val ratio: Float = correctCount.toFloat() / totalCount.toFloat()
    return when {
        ratio >= 1f -> ResultScoreBand.Perfect
        ratio >= 0.7f -> ResultScoreBand.Good
        else -> ResultScoreBand.Retry
    }
}

@Composable
fun ResultHeroHeadline(
    band: ResultScoreBand,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color? = null,
) {
    var started: Boolean by remember(band) { mutableStateOf(false) }
    LaunchedEffect(band) { started = true }
    val scale: Float by animateFloatAsState(
        targetValue = if (started) 1f else 0.8f,
        animationSpec = tween(durationMillis = 400),
        label = "resultHeroScale",
    )
    val textRes: Int = when (band) {
        ResultScoreBand.Perfect -> R.string.result_hero_perfect
        ResultScoreBand.Good -> R.string.result_hero_good
        ResultScoreBand.Retry -> R.string.result_hero_retry
    }
    val textColor: Color = color ?: when (band) {
        ResultScoreBand.Perfect, ResultScoreBand.Good -> MaterialTheme.colorScheme.primary
        ResultScoreBand.Retry -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        text = stringResource(textRes),
        style = style,
        color = textColor,
        textAlign = TextAlign.Center,
    )
}

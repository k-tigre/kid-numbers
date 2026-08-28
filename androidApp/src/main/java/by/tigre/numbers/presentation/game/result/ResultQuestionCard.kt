package by.tigre.numbers.presentation.game.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import by.tigre.numbers.R
import by.tigre.numbers.entity.GameOptions.Question.Equation
import by.tigre.numbers.entity.GameOptions.Question.Operation
import by.tigre.numbers.entity.GameResult
import by.tigre.tools.tools.platform.compose.AppShapes
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ColorFamily
import by.tigre.tools.tools.platform.compose.Dimens
import by.tigre.tools.tools.platform.compose.LocalGameColorsPalette

@Composable
fun ResultQuestionCard(
    result: GameResult.Result,
    modifier: Modifier = Modifier,
) {
    val colors: ColorFamily = when {
        !result.countsForScore -> ColorFamily(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            onColor = MaterialTheme.colorScheme.onSurfaceVariant,
            colorContainer = MaterialTheme.colorScheme.surfaceContainerHigh,
            onColorContainer = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        result.isCorrect -> LocalGameColorsPalette.current.gameSuccess
        else -> LocalGameColorsPalette.current.gameFailed
    }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = colors.colorContainer),
    ) {
        Column(modifier = Modifier.padding(Dimens.md)) {
            Text(
                text = when (result.question) {
                    is Equation.Double -> result.question.title
                    is Equation.Single -> result.question.title.format(
                        if (result.answer != null) result.question.x.toString() else "***",
                    )
                    is Operation -> result.question.title.format(
                        if (result.answer != null) result.question.x.toString() else "***",
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                color = colors.onColorContainer,
            )
            Text(
                modifier = Modifier.padding(top = Dimens.xs),
                style = MaterialTheme.typography.bodyMedium,
                text = when (result.question) {
                    is Equation.Double -> stringResource(
                        R.string.screen_game_result_item_user_answer_xy,
                        result.answer?.toString() ?: "-",
                        result.answerY?.toString() ?: "-",
                    )
                    else -> stringResource(
                        R.string.screen_game_result_item_user_answer,
                        result.answer?.toString() ?: "-",
                    )
                },
                color = colors.onColorContainer.copy(alpha = 0.85f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultQuestionCardPreview() {
    AppTheme {
        ResultQuestionCard(
            result = GameResult.Result(
                isCorrect = true,
                question = Operation.Multiplication(3, 4),
                answer = 12,
            ),
            modifier = Modifier.padding(Dimens.md),
        )
    }
}

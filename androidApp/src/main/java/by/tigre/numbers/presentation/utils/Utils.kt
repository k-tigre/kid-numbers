package by.tigre.numbers.presentation.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings.Equations
import by.tigre.numbers.entity.GameType
import java.text.SimpleDateFormat
import java.util.Locale

val TIME_FORMAT = SimpleDateFormat("m:ss", Locale.US)

@Composable
fun Difficult.toLabel(): String = stringResource(
    when (this) {
        Difficult.Easy -> R.string.screen_game_difficult_easy
        Difficult.Medium -> R.string.screen_game_difficult_medium
        Difficult.Hard -> R.string.screen_game_difficult_hard
    }
)

@Composable
fun GameType.toLabel(): String = stringResource(
    when (this) {
        GameType.Additional -> R.string.screen_game_type_addition
        GameType.Multiplication -> R.string.screen_game_type_multiplication
        GameType.Subtraction -> R.string.screen_game_type_subtraction
        GameType.Division -> R.string.screen_game_type_division
        GameType.Equations -> R.string.screen_game_type_equations
    }
)

@Composable
fun Challenge.Duration.toLabel(): String = stringResource(
    when (this) {
        Challenge.Duration.TenMinutes -> R.string.screen_challenge_settings_duration_10_minutes
        Challenge.Duration.HalfHour -> R.string.screen_challenge_settings_duration_30_minutes
        Challenge.Duration.OneHour -> R.string.screen_challenge_settings_duration_1_hour
        Challenge.Duration.OneDay -> R.string.screen_challenge_settings_duration_1_day
        Challenge.Duration.OneWeek -> R.string.screen_challenge_settings_duration_1_week
    }
)

@Composable
fun Equations.Type.toLabel(): String = stringResource(
    when (this) {
        Equations.Type.Additional -> R.string.screen_game_type_addition
        Equations.Type.Multiplication -> R.string.screen_game_type_multiplication
        Equations.Type.Both -> R.string.screen_game_type_addition_and_multiplication
    }
)

@Composable
fun SelectableButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    if (isSelected) {
        Button(modifier = modifier, onClick = onClick, content = content)
    } else {
        ElevatedButton(modifier = modifier, onClick = onClick, content = content)
    }
}

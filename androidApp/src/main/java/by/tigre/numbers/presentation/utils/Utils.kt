package by.tigre.numbers.presentation.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
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

//@Composable
//fun Equations.Type.toLabel(): String = stringResource(
//    when (this) {
//        Equations.Type.Additional -> R.string.screen_game_type_addition
//        Equations.Type.Multiplication -> R.string.screen_game_type_multiplication
//        Equations.Type.Both -> R.string.screen_game_type_addition_and_multiplication
//    }
//)

@Composable
fun Equations.Type.toLabel(): String =
    when (this) {
        Equations.Type.Additional -> "A + X = B"
        Equations.Type.Multiplication -> "A * X = B"
        Equations.Type.Both -> "A + B * X = C"
    }

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

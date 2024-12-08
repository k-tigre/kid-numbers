package by.tigre.numbers.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
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
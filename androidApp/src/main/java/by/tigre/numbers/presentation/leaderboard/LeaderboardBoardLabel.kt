package by.tigre.numbers.presentation.leaderboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.presentation.utils.toLabel

@Composable
fun GameSettings.toLeaderboardBoardLabel(): String = when (this) {
    is GameSettings.Multiplication -> {
        val numbers: String = selectedNumbers.sorted().joinToString(", ")
        if (isPositive) {
            stringResource(R.string.screen_leaderboard_board_mult, numbers, difficult.toLabel())
        } else {
            stringResource(R.string.screen_leaderboard_board_div, numbers, difficult.toLabel())
        }
    }
    is GameSettings.Additional -> {
        val rangeLabel: String = if (range.withNegative) {
            stringResource(R.string.screen_leaderboard_board_range_signed, range.max)
        } else {
            stringResource(R.string.screen_leaderboard_board_range_positive, range.max)
        }
        if (isPositive) {
            stringResource(R.string.screen_leaderboard_board_add, rangeLabel, difficult.toLabel())
        } else {
            stringResource(R.string.screen_leaderboard_board_sub, rangeLabel, difficult.toLabel())
        }
    }
    is GameSettings.Equations -> {
        val rangeLabel: String = if (range.withNegative) {
            stringResource(R.string.screen_leaderboard_board_range_signed, range.max)
        } else {
            stringResource(R.string.screen_leaderboard_board_range_positive, range.max)
        }
        stringResource(
            R.string.screen_leaderboard_board_eq,
            rangeLabel,
            type.toLabel(),
            dimension.toLabel(),
            difficult.toLabel(),
        )
    }
}

@Composable
private fun GameSettings.Equations.Type.toLabel(): String = when (this) {
    GameSettings.Equations.Type.Additional -> stringResource(R.string.screen_leaderboard_board_eq_type_add)
    GameSettings.Equations.Type.Multiplication -> stringResource(R.string.screen_leaderboard_board_eq_type_mult)
    GameSettings.Equations.Type.Both -> stringResource(R.string.screen_leaderboard_board_eq_type_both)
}

@Composable
private fun GameSettings.Equations.Dimension.toLabel(): String = when (this) {
    GameSettings.Equations.Dimension.Single -> stringResource(R.string.screen_leaderboard_board_eq_dim_single)
    GameSettings.Equations.Dimension.Double -> stringResource(R.string.screen_leaderboard_board_eq_dim_double)
}

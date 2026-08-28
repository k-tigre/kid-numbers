package by.tigre.numbers.presentation.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.ComposableView
import by.tigre.tools.tools.platform.compose.view.EmptyScreen
import by.tigre.tools.tools.platform.compose.view.ErrorScreen

class LeaderboardView(
    private val component: LeaderboardComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        val state by component.uiState.collectAsState()
        Column(modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = component::onBack) {
                    Icon(painter = painterResource(id = R.drawable.baseline_close_24), contentDescription = null)
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.screen_leaderboard_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = component::onRefresh) {
                    Text(text = "↻")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedTab == LeaderboardTab.Speed,
                    onClick = { component.onTabSelected(LeaderboardTab.Speed) },
                    label = { Text(stringResource(R.string.screen_leaderboard_tab_speed)) },
                )
                FilterChip(
                    selected = state.selectedTab == LeaderboardTab.Total,
                    onClick = { component.onTabSelected(LeaderboardTab.Total) },
                    label = { Text(stringResource(R.string.screen_leaderboard_tab_total)) },
                )
            }
            state.backfillSubmittedCount?.let { count ->
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = stringResource(R.string.screen_leaderboard_backfill_done, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            when (state.selectedTab) {
                LeaderboardTab.Speed -> state.speedBoardSettings?.let { settings ->
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        text = settings.toLeaderboardBoardLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                LeaderboardTab.Total -> Unit
            }
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp),
                    )
                }
                state.errorMessage != null -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ErrorScreen(
                            title = stringResource(R.string.screen_leaderboard_load_error),
                            message = state.errorMessage ?: stringResource(R.string.screen_state_error_network_message),
                            actionTitle = stringResource(R.string.screen_state_retry),
                            retryAction = component::onRefresh,
                        )
                    }
                }
                state.selectedTab == LeaderboardTab.Speed && state.speedBoardSettings == null -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        EmptyScreen(
                            message = stringResource(R.string.screen_leaderboard_speed_no_board),
                        )
                    }
                }
                state.selectedTab == LeaderboardTab.Speed && state.speedEntries.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        EmptyScreen(
                            title = stringResource(R.string.screen_state_empty_leaderboard_title),
                            message = stringResource(R.string.screen_state_empty_leaderboard_message),
                        )
                    }
                }
                state.selectedTab == LeaderboardTab.Total && state.totalEntries.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        EmptyScreen(
                            title = stringResource(R.string.screen_state_empty_leaderboard_title),
                            message = stringResource(R.string.screen_state_empty_leaderboard_message),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        when (state.selectedTab) {
                            LeaderboardTab.Speed -> itemsIndexed(state.speedEntries) { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.screen_leaderboard_speed_item,
                                            index + 1,
                                            entry.nickname,
                                            TIME_FORMAT.format(entry.bestTimeSeconds * 1000L),
                                        ),
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (entry.mistakes > 0) {
                                        Text(
                                            text = stringResource(R.string.screen_leaderboard_speed_item_mistakes, entry.mistakes),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                            LeaderboardTab.Total -> itemsIndexed(state.totalEntries) { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.screen_leaderboard_item,
                                            index + 1,
                                            entry.nickname,
                                            entry.totalScore,
                                        ),
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = stringResource(R.string.screen_leaderboard_item_games, entry.gamesCount),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

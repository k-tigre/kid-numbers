package by.tigre.numbers.presentation.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import by.tigre.tools.tools.platform.compose.view.EmptyScreen
import by.tigre.tools.tools.platform.compose.view.ErrorScreen
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize

class LeaderboardView(
    private val component: LeaderboardComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_leaderboard_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onBack),
        actions = {
            listOf(
                ToolbarConfig.Action.Icon(
                    vector = Icons.Default.Refresh,
                    action = component::onRefresh,
                )
            )
        },
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val state by component.uiState.collectAsState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ProgressIndicator(size = ProgressIndicatorSize.LARGE)
                        }
                    }
                    state.errorMessage != null -> {
                        ErrorScreen(
                            title = stringResource(R.string.screen_leaderboard_load_error),
                            message = state.errorMessage ?: stringResource(R.string.screen_state_error_network_message),
                            actionTitle = stringResource(R.string.screen_state_retry),
                            retryAction = component::onRefresh,
                        )
                    }
                    state.selectedTab == LeaderboardTab.Speed && state.speedBoardSettings == null -> {
                        EmptyScreen(
                            message = stringResource(R.string.screen_leaderboard_speed_no_board),
                        )
                    }
                    state.selectedTab == LeaderboardTab.Speed && state.speedEntries.isEmpty() -> {
                        EmptyScreen(
                            title = stringResource(R.string.screen_state_empty_leaderboard_title),
                            message = stringResource(R.string.screen_state_empty_leaderboard_message),
                        )
                    }
                    state.selectedTab == LeaderboardTab.Total && state.totalEntries.isEmpty() -> {
                        EmptyScreen(
                            title = stringResource(R.string.screen_state_empty_leaderboard_title),
                            message = stringResource(R.string.screen_state_empty_leaderboard_message),
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            when (state.selectedTab) {
                                LeaderboardTab.Speed -> itemsIndexed(state.speedEntries) { index, entry ->
                                    LeaderboardRow(
                                        rank = index + 1,
                                        nickname = entry.nickname,
                                        score = TIME_FORMAT.format(entry.bestTimeSeconds * 1000L),
                                        timeLabel = if (entry.mistakes > 0) {
                                            stringResource(R.string.screen_leaderboard_speed_item_mistakes, entry.mistakes)
                                        } else {
                                            null
                                        },
                                        isCurrentUser = entry.nickname == state.currentNickname,
                                    )
                                }
                                LeaderboardTab.Total -> itemsIndexed(state.totalEntries) { index, entry ->
                                    LeaderboardRow(
                                        rank = index + 1,
                                        nickname = entry.nickname,
                                        score = stringResource(R.string.screen_leaderboard_points, entry.totalScore),
                                        timeLabel = stringResource(R.string.screen_leaderboard_item_games, entry.gamesCount),
                                        isCurrentUser = entry.nickname == state.currentNickname,
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

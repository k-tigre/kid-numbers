package by.tigre.numbers.presentation.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.ComposableView

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
                Difficult.entries.forEach { difficult ->
                    FilterChip(
                        selected = state.selectedDifficult == difficult,
                        onClick = { component.onDifficultSelected(difficult) },
                        label = { Text(difficult.toLabel()) },
                    )
                }
            }
            state.backfillSubmittedCount?.let { count ->
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = stringResource(R.string.screen_leaderboard_backfill_done, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
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
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = state.errorMessage ?: stringResource(R.string.screen_leaderboard_load_error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                state.entries.isEmpty() -> {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.screen_leaderboard_empty),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        itemsIndexed(state.entries) { index, entry ->
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

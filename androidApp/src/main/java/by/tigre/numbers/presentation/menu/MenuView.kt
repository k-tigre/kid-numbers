package by.tigre.numbers.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.ComposableView

class MenuView(
    private val component: MenuComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        val leaderboardEnabled = component.isLeaderboardEnabled.collectAsState().value
        LazyColumn(
            modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                DrawChallengeItem()
            }

            component.gameTypes.forEach { type ->
                item {
                    DrawItem(
                        title = stringResource(R.string.main_manu_learn, type.toLabel()),
                        action = { component.onGameClicked(type) }
                    )
                }
            }

            if (leaderboardEnabled) {
                item {
                    DrawItem(
                        title = stringResource(R.string.main_menu_leaderboard),
                        action = component::onLeaderboardClicked
                    )
                }
            }

            item {
                DrawItem(
                    title = stringResource(R.string.main_menu_statistic),
                    action = component::onStatisticClicked
                )
            }

            item {
                DrawItem(
                    title = stringResource(R.string.main_menu_history),
                    action = component::onHistoryClicked
                )
            }
        }
    }

    @Composable
    private fun DrawItem(modifier: Modifier = Modifier, title: String, action: () -> Unit) {
        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = action
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    private fun DrawChallengeItem(modifier: Modifier = Modifier) {
        val hasActiveChallenge = component.hasActiveChallenge.collectAsState().value
        Button(
            modifier = modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            onClick = component::onChallengeClicked
        ) {
            if (hasActiveChallenge) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.main_menu_challenges),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.main_menu_challenges_active),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.main_menu_challenges),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

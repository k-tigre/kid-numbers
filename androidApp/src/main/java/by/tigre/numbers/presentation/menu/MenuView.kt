package by.tigre.numbers.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.ComposableView

class MenuView(
    private val component: MenuComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        LazyColumn(
            modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
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
            modifier = modifier
                .padding(16.dp),
            onClick = action
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }

    @Composable
    private fun DrawChallengeItem(modifier: Modifier = Modifier) {
        val hasActiveChallenge = component.hasActiveChallenge.collectAsState().value
        Button(
            modifier = modifier
                .padding(16.dp),
            onClick = component::onChallengeClicked
        ) {
            Text(
                text = if (hasActiveChallenge) stringResource(R.string.main_menu_active_challenges) else stringResource(R.string.main_menu_no_active_challenges),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

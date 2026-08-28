package by.tigre.numbers.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.tools.tools.platform.compose.ScreenComposableView

class SettingsView(
    private val component: SettingsComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_settings_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(
            contentDescription = { stringResource(R.string.content_desc_back) },
            action = component::onCloseClicked,
        ),
    ),
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val state by component.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val savedMessage = stringResource(R.string.screen_settings_saved)
        LaunchedEffect(state.saved) {
            if (state.saved) {
                snackbarHostState.showSnackbar(savedMessage)
            }
        }
        Scaffold(
            modifier = Modifier.padding(innerPadding),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { scaffoldPadding ->
            Column(
                modifier = Modifier
                    .padding(scaffoldPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.screen_settings_leaderboard_nickname_hint),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    value = state.nickname,
                    onValueChange = component::onNicknameChanged,
                    label = { Text(stringResource(R.string.screen_leaderboard_submit_nickname)) },
                    singleLine = true,
                )
                state.error?.let { error ->
                    val message = when (error) {
                        SettingsUiState.SettingsError.EmptyNickname ->
                            stringResource(R.string.screen_leaderboard_submit_error_empty_nickname)
                    }
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                TextButton(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = component::onSaveClicked,
                ) {
                    Text(stringResource(R.string.screen_settings_save))
                }
            }
        }
    }
}

package by.tigre.numbers.presentation.root

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.challenge.RootChallengeView
import by.tigre.numbers.presentation.game.RootChallengeGameView
import by.tigre.numbers.presentation.game.RootGameView
import by.tigre.numbers.presentation.history.HistoryView
import by.tigre.numbers.presentation.leaderboard.LeaderboardView
import by.tigre.numbers.presentation.menu.MenuView
import by.tigre.numbers.presentation.settings.SettingsView
import by.tigre.numbers.presentation.statistic.StatisticView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class RootView(
    private val component: RootComponent,
) : ComposableView {

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun Draw(modifier: Modifier) {
        val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            null
        }

        Children(
            modifier = modifier,
            stack = component.pages,
            animation = stackAnimation(animator = fade())
        ) {
            when (val child = it.instance) {
                is RootComponent.PageChild.Menu -> MenuView(child.component)
                is RootComponent.PageChild.Game -> RootGameView(child.component)
                is RootComponent.PageChild.History -> HistoryView(child.component)
                is RootComponent.PageChild.Statistic -> StatisticView(child.component)
                is RootComponent.PageChild.Leaderboard -> LeaderboardView(child.component)
                is RootComponent.PageChild.Settings -> SettingsView(child.component)
                is RootComponent.PageChild.Challenge -> RootChallengeView(child.component)
                is RootComponent.PageChild.GameChallenge -> RootChallengeGameView(child.component)
            }.Draw(
                modifier = Modifier
                    .safeDrawingPadding()
                    .fillMaxSize()
            )
        }

        if (permissionState?.status?.isGranted == false) {
            PermissionsRequest(permissionState)
        }

        DrawReminderDialog()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun PermissionsRequest(permissionState: PermissionState) {
        LaunchedEffect("permissions") { permissionState.launchPermissionRequest() }
    }

    @Composable
    private fun DrawReminderDialog() {
        val dialogSlot by component.dialogs.subscribeAsState()
        dialogSlot.child?.instance?.also {

            when (it) {
                is RootComponent.DialogChild.Reminder -> {
                    AlertDialog(
                        onDismissRequest = component::onDismissDialog,
                        title = { Text(text = stringResource(R.string.dialog_reminder_start_challenges_title)) },
                        text = {
                            Text(
                                text = stringResource(R.string.dialog_reminder_start_challenges_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    component.onStartChallengeFromReminder(it.challengeId)
                                }
                            ) {
                                Text(stringResource(R.string.button_start))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    component.onReminderLaterClicked(true)
                                }
                            ) {
                                Text(stringResource(R.string.dialog_reminder_show_challenges_button_cancel))
                            }
                        },
                        modifier = Modifier.width(300.dp),
                    )
                }

                RootComponent.DialogChild.ReminderHasChallenge -> {
                    AlertDialog(
                        onDismissRequest = component::onDismissDialog,
                        title = { Text(text = stringResource(R.string.dialog_reminder_show_challenges_title)) },
                        text = {
                            Text(
                                text = stringResource(R.string.dialog_reminder_show_challenges_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    component.onShowChallenges()
                                }
                            ) {
                                Text(stringResource(R.string.dialog_reminder_show_challenges_button_show))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    component.onReminderLaterClicked(false)
                                }
                            ) {
                                Text(stringResource(R.string.dialog_reminder_show_challenges_button_cancel))
                            }
                        },
                        modifier = Modifier.width(300.dp),
                    )
                }
            }
        }
    }
}

package by.tigre.numbers.presentation.root

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.challenge.RootChallengeView
import by.tigre.numbers.presentation.game.RootChallengeGameView
import by.tigre.numbers.presentation.game.RootGameView
import by.tigre.numbers.presentation.history.HistoryView
import by.tigre.numbers.presentation.menu.MenuView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
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
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun PermissionsRequest(permissionState: PermissionState) {
        LaunchedEffect("permissions") { permissionState.launchPermissionRequest() }
    }
}

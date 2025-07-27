package by.tigre.numbers.presentation.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.result.ResultView
import by.tigre.numbers.presentation.game.settings.AdditionalSettingsView
import by.tigre.numbers.presentation.game.settings.EquationsSettingsView
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

class RootGameView(
    private val component: RootGameComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Children(
            modifier = modifier,
            stack = component.pages,
            animation = stackAnimation(animator = fade())
        ) {
            when (val child = it.instance) {
                is RootGameComponent.PageChild.SettingsAdditional -> AdditionalSettingsView(
                    component = child.component,
                    confirmTitle = stringResource(R.string.screen_game_settings_start)
                )

                is RootGameComponent.PageChild.SettingsMultiplication -> MultiplicationSettingsView(
                    component = child.component,
                    confirmTitle = stringResource(R.string.screen_game_settings_start)
                )

                is RootGameComponent.PageChild.SettingsEquations -> EquationsSettingsView(
                    component = child.component,
                    confirmTitle = stringResource(R.string.screen_game_settings_start)
                )

                is RootGameComponent.PageChild.Game -> GameView(child.component)
                is RootGameComponent.PageChild.Result -> ResultView(child.component)
            }.Draw(modifier = Modifier.fillMaxSize())
        }
    }
}

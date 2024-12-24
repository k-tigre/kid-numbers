package by.tigre.numbers.presentation.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.game.result.ResultView
import by.tigre.numbers.presentation.game.settings.AdditionalSettingsView
import by.tigre.numbers.presentation.game.settings.EquationsSettingsView
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation

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
                is RootGameComponent.PageChild.SettingsAdditional -> AdditionalSettingsView(child.component)
                is RootGameComponent.PageChild.SettingsMultiplication -> MultiplicationSettingsView(child.component)
                is RootGameComponent.PageChild.SettingsEquations -> EquationsSettingsView(child.component)
                is RootGameComponent.PageChild.Game -> GameView(child.component)
                is RootGameComponent.PageChild.Result -> ResultView(child.component)
            }.Draw(modifier = Modifier.fillMaxSize())
        }
    }
}

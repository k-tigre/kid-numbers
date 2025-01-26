package by.tigre.numbers.presentation.challenge.creator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.settings.AdditionalSettingsView
import by.tigre.numbers.presentation.game.settings.EquationsSettingsView
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation

class RootDetailsView(
    private val component: DetailsComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Children(
            modifier = modifier,
            stack = component.pages,
            animation = stackAnimation(animator = fade())
        ) {
            when (val child = it.instance) {
                is DetailsComponent.PageChild.TaskList -> ChallengeTaskView(component)
                is DetailsComponent.PageChild.SettingsAdditional -> AdditionalSettingsView(
                    component = child.component,
                    confirmTitle = stringResource(R.string.screen_challenge_settings_add)
                )

                is DetailsComponent.PageChild.SettingsMultiplication -> MultiplicationSettingsView(
                    component = child.component,
                    confirmTitle = stringResource(R.string.screen_challenge_settings_add)
                )

                is DetailsComponent.PageChild.SettingsEquations -> EquationsSettingsView(
                    component = child.component,
                    confirmTitle = stringResource(R.string.screen_challenge_settings_add)
                )
            }.Draw(modifier = Modifier.fillMaxSize())
        }
    }


}

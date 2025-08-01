package by.tigre.numbers.presentation.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
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

class RootView(
    private val component: RootComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
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
            }.Draw(modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize())
        }
    }
}

package by.tigre.numbers.presentation.smart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.game.GameView
import by.tigre.numbers.presentation.game.result.ResultView
import by.tigre.tools.tools.platform.compose.ComposableView
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

class RootSmartGameView(
    private val component: RootSmartGameComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Children(
            modifier = modifier,
            stack = component.pages,
            animation = stackAnimation(animator = slide() + fade()),
        ) {
            when (val child = it.instance) {
                RootSmartGameComponent.PageChild.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ProgressIndicator(size = ProgressIndicatorSize.LARGE)
                    }
                }
                is RootSmartGameComponent.PageChild.Game -> GameView(child.component).Draw(Modifier.fillMaxSize())
                is RootSmartGameComponent.PageChild.Result -> ResultView(child.component).Draw(modifier.fillMaxSize())
            }
        }
    }
}

package by.tigre.numbers.presentation.challenge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.challenge.creator.RootDetailsView
import by.tigre.numbers.presentation.challenge.list.ListView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation

class RootChallengeView(
    private val component: RootChallengeComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Children(
            modifier = modifier,
            stack = component.pages,
            animation = stackAnimation(animator = fade())
        ) {
            when (val child = it.instance) {
                is RootChallengeComponent.PageChild.List -> ListView(child.component)
                is RootChallengeComponent.PageChild.Details -> RootDetailsView(child.component)
            }.Draw(modifier = Modifier.fillMaxSize())
        }
    }
}

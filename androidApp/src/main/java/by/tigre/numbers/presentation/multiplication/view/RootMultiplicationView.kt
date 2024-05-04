package by.tigre.numbers.presentation.multiplication.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.multiplication.component.RootMultiplicationComponent
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation

class RootMultiplicationView(
    private val component: RootMultiplicationComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Children(
            modifier = modifier,
            stack = component.pages,
            animation = stackAnimation(animator = fade())
        ) {
            when (val child = it.instance) {
                is RootMultiplicationComponent.PageChild.Settings -> MultiplicationSettingsView(child.component)
                is RootMultiplicationComponent.PageChild.Game -> MultiplicationGameView(child.component)
                is RootMultiplicationComponent.PageChild.Result -> MultiplicationResultView(child.component)
            }.Draw(modifier = Modifier.fillMaxSize())
        }
    }
}

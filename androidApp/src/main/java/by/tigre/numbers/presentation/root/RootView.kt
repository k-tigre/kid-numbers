package by.tigre.numbers.presentation.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.additional.RootAdditionalView
import by.tigre.numbers.presentation.menu.MenuView
import by.tigre.numbers.presentation.multiplication.view.RootMultiplicationView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation

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
                is RootComponent.PageChild.Multiplication -> RootMultiplicationView(child.component)
                is RootComponent.PageChild.Additional -> RootAdditionalView(child.component)
            }.Draw(modifier = Modifier.fillMaxSize())
        }
    }
}

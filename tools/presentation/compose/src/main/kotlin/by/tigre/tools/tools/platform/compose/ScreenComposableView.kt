package by.tigre.tools.tools.platform.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

abstract class ScreenComposableView(private val config: ToolbarConfig) : ComposableView {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Draw(modifier: Modifier) {
        Scaffold(
            modifier = modifier,
            topBar = {
                Box(Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            DrawToolbar()
                        },
                        navigationIcon = {
                            config.navigationIcon?.let { icon ->
                                IconButton(
                                    onClick = icon.action
                                ) {
                                    Icon(
                                        imageVector = icon.vector,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        actions = {
                            DrawActions()
                        }
                    )

                    HorizontalDivider(Modifier.align(Alignment.BottomCenter))
                }
            },
        ) { innerPadding ->
            DrawContent(innerPadding)
        }
    }

    @Composable
    open fun DrawToolbar() {
        Text(
            modifier = Modifier,
            text = config.title(),
            textAlign = TextAlign.Center
        )
    }

    @Composable
    open fun RowScope.DrawActions() {
        config.actions().forEach { action ->
            when (action) {
                is ToolbarConfig.Action.Icon -> {
                    IconButton(
                        onClick = action.action,
                        enabled = action.enabled
                    ) {
                        Icon(
                            imageVector = action.vector,
                            contentDescription = null
                        )
                    }
                }

                is ToolbarConfig.Action.Text -> {
                    TextButton(
                        onClick = action.action,
                        enabled = action.enabled
                    ) {
                        Text(action.title)
                    }
                }
            }
        }
    }

    @Composable
    abstract fun DrawContent(innerPadding: PaddingValues)

    class ToolbarConfig(
        val title: @Composable () -> String,
        val navigationIcon: NavigationIconAction? = null,
        val actions: @Composable () -> List<Action> = { emptyList() }
    ) {

        data class NavigationIconAction(val vector: ImageVector = Icons.AutoMirrored.Filled.ArrowBack, val action: () -> Unit)
        sealed interface Action {

            data class Text(val title: String, val enabled: Boolean = true, val action: () -> Unit) : Action
            data class Icon(val vector: ImageVector, val enabled: Boolean = true, val action: () -> Unit) : Action
        }

    }
}

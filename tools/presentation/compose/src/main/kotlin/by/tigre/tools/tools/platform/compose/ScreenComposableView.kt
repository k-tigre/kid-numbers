package by.tigre.tools.tools.platform.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

abstract class ScreenComposableView(private val config: ToolbarConfig) : ComposableView {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Draw(modifier: Modifier) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        DrawToolbar()
                    }
                )
            },
        ) { innerPadding ->
            DrawContent(innerPadding)
        }
    }

    @Composable
    open fun DrawToolbar() {
        when (config) {
            is ToolbarConfig.Default -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = config.onBackClicked
                    ) {
                        Icon(
                            painter = painterResource(by.tigre.numberscompose.R.drawable.baseline_arrow_back_24),
                            contentDescription = null
                        )
                    }

                    Text(
                        modifier = Modifier,
                        text = config.title(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Composable
    abstract fun DrawContent(innerPadding: PaddingValues)

    sealed interface ToolbarConfig {
        class Default(val title: @Composable () -> String, val onBackClicked: () -> Unit) : ToolbarConfig
    }
}

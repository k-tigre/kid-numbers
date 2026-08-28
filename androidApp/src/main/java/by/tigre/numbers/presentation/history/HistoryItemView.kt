package by.tigre.numbers.presentation.history

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.numbers.presentation.game.result.ResultScreenContent
import by.tigre.tools.tools.platform.compose.ScreenComposableView

class HistoryItemView(
    private val component: ResultComponent,
    onBack: () -> Unit
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_history_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = onBack),
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val result by component.results.collectAsState()
        ResultScreenContent(result = result, innerPadding = innerPadding)
    }
}

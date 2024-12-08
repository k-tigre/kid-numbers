package by.tigre.numbers.presentation.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.tools.tools.platform.compose.ComposableView

class MenuView(
    private val component: MenuComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(
            modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DrawItem(
                title = stringResource(R.string.main_manu_learn_addition),
                action = component::onAdditionClicked
            )
            DrawItem(
                title = stringResource(R.string.main_manu_lean_multiplication),
                action = component::onMultiplicationClicked
            )
            DrawItem(
                title = stringResource(R.string.main_manu_learn_subtraction),
                action = component::onSubtractionClicked
            )
            DrawItem(
                title = stringResource(R.string.main_menu_learn_division),
                action = component::onDivisionClicked
            )
            DrawItem(
                title = stringResource(R.string.main_menu_history),
                action = component::onHistoryClicked
            )
        }
    }

    @Composable
    private fun DrawItem(modifier: Modifier = Modifier, title: String, action: () -> Unit) {
        Button(
            modifier = modifier
                .padding(16.dp),
            onClick = action
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

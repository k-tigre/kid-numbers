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
import androidx.compose.ui.unit.dp
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
            Button(
                modifier = Modifier
                    .padding(16.dp),
                onClick = component::onMultiplicationClicked
            ) {
                Text(
                    text = "Учить умножение",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }

            Button(
                modifier = Modifier
                    .padding(16.dp),
                onClick = component::onAdditionClicked
            ) {
                Text(
                    text = "Учить сложение",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

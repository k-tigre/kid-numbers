package by.tigre.numbers.presentation.additional

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import by.tigre.tools.tools.platform.compose.ComposableView

class RootAdditionalView(
    private val component: RootAdditionalComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(
            modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Учим сложение",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

package by.tigre.tools.tools.platform.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numberscompose.R
import by.tigre.tools.tools.platform.compose.AppTheme

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.screen_state_empty_title),
    message: String,
    actionTitle: String = stringResource(R.string.reload_action),
    reloadAction: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            painter = painterResource(R.drawable.ic_baseline_error_outline_24),
            contentDescription = "error",
            modifier = Modifier.size(64.dp),
        )

        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            onClick = reloadAction,
        ) {
            Text(
                text = actionTitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyScreenPreview() {
    AppTheme {
        EmptyScreen(
            title = "Ничего не удалось найти",
            message = "Попробуйте повторить",
            reloadAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyScreenPreview2() {
    AppTheme {
        EmptyScreen(
            reloadAction = {},
            message = "Попробуйте повторить",
        )
    }
}

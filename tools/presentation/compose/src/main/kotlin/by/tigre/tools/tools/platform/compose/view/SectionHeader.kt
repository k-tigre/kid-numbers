package by.tigre.tools.tools.platform.compose.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.tools.tools.platform.compose.AppTheme

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreviewLight() {
    AppTheme(darkTheme = false) {
        SectionHeader(
            title = "Practice",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreviewDark() {
    AppTheme(darkTheme = true) {
        SectionHeader(
            title = "Practice",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

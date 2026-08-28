package by.tigre.tools.tools.platform.compose.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.Dimens

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.lg, bottom = Dimens.sm),
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
            modifier = Modifier.padding(horizontal = Dimens.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreviewDark() {
    AppTheme(darkTheme = true) {
        SectionHeader(
            title = "Practice",
            modifier = Modifier.padding(horizontal = Dimens.md),
        )
    }
}

package by.tigre.tools.tools.platform.compose.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import by.tigre.tools.tools.platform.compose.AppShapes
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.Dimens

@Composable
fun MenuCard(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.menuCardMinHeight),
        shape = AppShapes.menuCard,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.sm + Dimens.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.lg),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(Dimens.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuCardPreviewLight() {
    AppTheme(darkTheme = false) {
        MenuCard(
            title = "Addition",
            subtitle = "Practice sums",
            icon = rememberVectorPainter(Icons.Filled.Star),
            onClick = {},
            modifier = Modifier.padding(Dimens.md),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuCardPreviewDark() {
    AppTheme(darkTheme = true) {
        MenuCard(
            title = "Addition",
            icon = rememberVectorPainter(Icons.Filled.Star),
            onClick = {},
            modifier = Modifier.padding(Dimens.md),
        )
    }
}

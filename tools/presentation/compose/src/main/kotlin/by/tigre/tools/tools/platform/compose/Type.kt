package by.tigre.tools.tools.platform.compose

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import by.tigre.numberscompose.R

// Nunito variable font (OFL license) — https://fonts.google.com/specimen/Nunito
private val NunitoFontFamily: FontFamily = FontFamily(
    Font(R.font.nunito, FontWeight.Normal),
    Font(R.font.nunito, FontWeight.Medium),
    Font(R.font.nunito, FontWeight.SemiBold),
    Font(R.font.nunito, FontWeight.Bold),
)

private fun TextStyle.withNunito(): TextStyle = copy(fontFamily = NunitoFontFamily)

val AppTypography: Typography = Typography().let { typography ->
    Typography(
        displayLarge = typography.displayLarge.withNunito(),
        displayMedium = typography.displayMedium.withNunito(),
        displaySmall = typography.displaySmall.withNunito(),
        headlineLarge = typography.headlineLarge.withNunito(),
        headlineMedium = typography.headlineMedium.withNunito(),
        headlineSmall = typography.headlineSmall.withNunito(),
        titleLarge = typography.titleLarge.withNunito(),
        titleMedium = typography.titleMedium.withNunito(),
        titleSmall = typography.titleSmall.withNunito(),
        bodyLarge = typography.bodyLarge.withNunito(),
        bodyMedium = typography.bodyMedium.withNunito(),
        bodySmall = typography.bodySmall.withNunito(),
        labelLarge = typography.labelLarge.withNunito(),
        labelMedium = typography.labelMedium.withNunito(),
        labelSmall = typography.labelSmall.withNunito(),
    )
}

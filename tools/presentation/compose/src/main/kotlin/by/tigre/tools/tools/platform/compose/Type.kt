package by.tigre.tools.tools.platform.compose

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import by.tigre.numberscompose.R

// Nunito static fonts (OFL license) — https://fonts.google.com/specimen/Nunito
private val NunitoFontFamily: FontFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

private fun TextStyle.withNunito(weight: FontWeight = fontWeight ?: FontWeight.Normal): TextStyle = copy(
    fontFamily = NunitoFontFamily,
    fontWeight = weight,
)

val AppTypography: Typography = Typography().let { typography ->
    Typography(
        displayLarge = typography.displayLarge.withNunito(FontWeight.Bold),
        displayMedium = typography.displayMedium.withNunito(FontWeight.Bold),
        displaySmall = typography.displaySmall.withNunito(FontWeight.Bold),
        headlineLarge = typography.headlineLarge.withNunito(FontWeight.Bold),
        headlineMedium = typography.headlineMedium.withNunito(FontWeight.SemiBold),
        headlineSmall = typography.headlineSmall.withNunito(FontWeight.SemiBold),
        titleLarge = typography.titleLarge.withNunito(FontWeight.SemiBold),
        titleMedium = typography.titleMedium.withNunito(FontWeight.SemiBold),
        titleSmall = typography.titleSmall.withNunito(FontWeight.Medium),
        bodyLarge = typography.bodyLarge.withNunito(FontWeight.Medium),
        bodyMedium = typography.bodyMedium.withNunito(FontWeight.Normal),
        bodySmall = typography.bodySmall.withNunito(FontWeight.Normal),
        labelLarge = typography.labelLarge.withNunito(FontWeight.SemiBold),
        labelMedium = typography.labelMedium.withNunito(FontWeight.Medium),
        labelSmall = typography.labelSmall.withNunito(FontWeight.Medium),
    )
}

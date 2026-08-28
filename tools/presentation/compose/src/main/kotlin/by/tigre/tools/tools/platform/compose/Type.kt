@file:OptIn(ExperimentalTextApi::class)

package by.tigre.tools.tools.platform.compose

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import by.tigre.numberscompose.R

// Nunito variable font (OFL license) — https://fonts.google.com/specimen/Nunito
private fun nunitoFont(weight: FontWeight): Font = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
    ),
)

private val NunitoFontFamily: FontFamily = FontFamily(
    nunitoFont(FontWeight.Normal),
    nunitoFont(FontWeight.Medium),
    nunitoFont(FontWeight.SemiBold),
    nunitoFont(FontWeight.Bold),
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
        headlineMedium = typography.headlineMedium.withNunito(FontWeight.Bold),
        headlineSmall = typography.headlineSmall.withNunito(FontWeight.SemiBold),
        titleLarge = typography.titleLarge.withNunito(FontWeight.SemiBold),
        titleMedium = typography.titleMedium.withNunito(FontWeight.SemiBold),
        titleSmall = typography.titleSmall.withNunito(FontWeight.SemiBold),
        bodyLarge = typography.bodyLarge.withNunito(FontWeight.Normal),
        bodyMedium = typography.bodyMedium.withNunito(FontWeight.Normal),
        bodySmall = typography.bodySmall.withNunito(FontWeight.Normal),
        labelLarge = typography.labelLarge.withNunito(FontWeight.SemiBold),
        labelMedium = typography.labelMedium.withNunito(FontWeight.SemiBold),
        labelSmall = typography.labelSmall.withNunito(FontWeight.Medium),
    )
}

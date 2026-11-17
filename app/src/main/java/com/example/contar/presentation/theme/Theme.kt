package com.example.contar.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme

data class DosisColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val brandGreen: Color,
    val nextDoseCardBg: Color,
    val nextDoseCardBorder: Color,
    val nextDoseBadgeText: Color,
    val nextDoseTimeText: Color,
    val nextDoseSubText: Color,
    val primaryButtonBg: Color,
    val primaryButtonText: Color,
    val secondaryButtonBg: Color,
    val secondaryButtonText: Color,
    val chipBg: Color,
    val chipText: Color,
    val successCircleBg: Color,
    val successCheckColor: Color,
    val inputCardBg: Color,
    val inputCardLabel: Color,
    val inputCardValue: Color,
    val inputCardIcon: Color
)

val DosisLightColors = DosisColors(
    isDark = false,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF3F6F4),
    surfaceVariant = Color(0xFFE8ECE9),
    textPrimary = Color(0xFF111815),
    textSecondary = Color(0xFF6B7770),
    brandGreen = Color(0xFF1B4332),
    nextDoseCardBg = Color(0xFFDDF7E8),
    nextDoseCardBorder = Color(0xFFCEEEDB),
    nextDoseBadgeText = Color(0xFF1B5E3C),
    nextDoseTimeText = Color(0xFF0D281E),
    nextDoseSubText = Color(0xFF2D4F3F),
    primaryButtonBg = Color(0xFF164E3D),
    primaryButtonText = Color(0xFFFFFFFF),
    secondaryButtonBg = Color(0xFFF0F4F1),
    secondaryButtonText = Color(0xFF164E3D),
    chipBg = Color(0xFFE6F9EE),
    chipText = Color(0xFF1B5E3C),
    successCircleBg = Color(0xFFDDF7E8),
    successCheckColor = Color(0xFF164E3D),
    inputCardBg = Color(0xFFF3F6F4),
    inputCardLabel = Color(0xFF7E8B83),
    inputCardValue = Color(0xFF111815),
    inputCardIcon = Color(0xFF164E3D)
)

val DosisDarkColors = DosisColors(
    isDark = true,
    background = Color(0xFF09110E),
    surface = Color(0xFF12241D),
    surfaceVariant = Color(0xFF1B3229),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF8A9991),
    brandGreen = Color(0xFF7DF3B8),
    nextDoseCardBg = Color(0xFF132E24),
    nextDoseCardBorder = Color(0xFF1D4737),
    nextDoseBadgeText = Color(0xFF7DF3B8),
    nextDoseTimeText = Color(0xFFFFFFFF),
    nextDoseSubText = Color(0xFFA2B6AC),
    primaryButtonBg = Color(0xFF7DF3B8),
    primaryButtonText = Color(0xFF062215),
    secondaryButtonBg = Color(0xFF15261F),
    secondaryButtonText = Color(0xFFFFFFFF),
    chipBg = Color(0xFF17362B),
    chipText = Color(0xFF7DF3B8),
    successCircleBg = Color(0xFF17382B),
    successCheckColor = Color(0xFF7DF3B8),
    inputCardBg = Color(0xFF12241D),
    inputCardLabel = Color(0xFF8A9991),
    inputCardValue = Color(0xFFFFFFFF),
    inputCardIcon = Color(0xFF7DF3B8)
)

val LocalDosisColors = staticCompositionLocalOf { DosisDarkColors }

@Composable
fun ContarTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val dosisColors = if (darkTheme) DosisDarkColors else DosisLightColors

    val colorScheme = MaterialTheme.colorScheme.copy(
        primary = dosisColors.primaryButtonBg,
        onPrimary = dosisColors.primaryButtonText,
        background = dosisColors.background,
        onBackground = dosisColors.textPrimary,
        surfaceContainer = dosisColors.surface,
        onSurface = dosisColors.textPrimary,
        onSurfaceVariant = dosisColors.textSecondary
    )

    CompositionLocalProvider(LocalDosisColors provides dosisColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

package com.dosis.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Esquema claro (modo día) ────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = DosisGreen,
    onPrimary = DosisOnGreen,
    primaryContainer = DosisGreenContainer,
    onPrimaryContainer = DosisGreenDark,
    secondary = DosisGreenLight,
    onSecondary = DosisOnGreen,
    secondaryContainer = DosisGreenContainer,
    onSecondaryContainer = DosisGreenDark,
    tertiary = DosisGreenLight,
    onTertiary = DosisOnGreen,
    background = DosisBackground,
    onBackground = DosisTextPrimary,
    surface = DosisSurface,
    onSurface = DosisTextPrimary,
    surfaceVariant = DosisBackground,
    onSurfaceVariant = DosisTextSecondary,
    error = DosisError,
    onError = DosisWhite,
    outline = DosisDivider,
    outlineVariant = DosisGreenContainer
)

// ── Esquema oscuro (modo noche) ─────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = DosisGreenDarkMode,
    onPrimary = DosisGreenDark,
    primaryContainer = DosisGreenDark,
    onPrimaryContainer = DosisGreenContainer,
    secondary = DosisGreenDarkMode,
    onSecondary = DosisGreenDark,
    background = DosisBackgroundDark,
    onBackground = DosisWhite,
    surface = DosisSurfaceDark,
    onSurface = DosisWhite,
    error = DosisError,
    onError = DosisWhite
)

@Composable
fun DosisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DosisTypography,
        content = content
    )
}

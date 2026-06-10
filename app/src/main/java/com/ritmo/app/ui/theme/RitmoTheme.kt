package com.ritmo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ritmo.app.data.preferences.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6B4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC5F1DD),
    onPrimaryContainer = Color(0xFF073B2D),
    secondary = Color(0xFF4F5F54),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E7DA),
    onSecondaryContainer = Color(0xFF213528),
    tertiary = Color(0xFF8A4B2B),
    tertiaryContainer = Color(0xFFFFDCC8),
    background = Color(0xFFF7FAF5),
    surface = Color(0xFFFFFBFE),
    surfaceContainer = Color(0xFFEEF4ED),
    surfaceContainerHigh = Color(0xFFE8EFE7),
    onSurface = Color(0xFF181D19),
    surfaceVariant = Color(0xFFDCE6DD),
    outlineVariant = Color(0xFFC0CAC1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8EDBB9),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513C),
    onPrimaryContainer = Color(0xFFC5F1DD),
    secondary = Color(0xFFBCCDBF),
    onSecondary = Color(0xFF27352B),
    secondaryContainer = Color(0xFF3D4C40),
    onSecondaryContainer = Color(0xFFD8E7DA),
    tertiary = Color(0xFFFFB78E),
    tertiaryContainer = Color(0xFF6D3618),
    background = Color(0xFF101511),
    surface = Color(0xFF101511),
    surfaceContainer = Color(0xFF1C241E),
    surfaceContainerHigh = Color(0xFF273029),
    onSurface = Color(0xFFE1E6E0),
    surfaceVariant = Color(0xFF404A42),
    outlineVariant = Color(0xFF404A42),
)

@Composable
fun RitmoTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}

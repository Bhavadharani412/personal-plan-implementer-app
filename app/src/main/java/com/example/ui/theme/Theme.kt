package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LightMintGreen,
    onPrimary = DarkForestGreen,
    secondary = AccentGreen,
    onSecondary = WhiteText,
    tertiary = GoldXPAccent,
    background = DarkBackground,
    onBackground = WhiteText,
    surface = DarkSurfaceElevated,
    onSurface = WhiteText,
    error = DangerRed,
    onError = WhiteText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = MutedText,
    outline = DarkBorder
)

@Composable
fun CompoundOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

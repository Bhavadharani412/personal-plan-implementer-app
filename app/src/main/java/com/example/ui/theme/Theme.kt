package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SkillSkyBlue,
    secondary = SkillTeal,
    tertiary = SkillLavender,
    background = SkillNightTime,
    surface = Color(0xFF1E2436),
    onBackground = Color(0xFFF8F7F4),
    onSurface = Color(0xFFF8F7F4)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SkillNightTime,
    secondary = SkillTeal,
    tertiary = SkillLavender,
    background = ColorBackground,
    surface = ColorCard,
    onBackground = SkillNightTime,
    onSurface = SkillNightTime
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force custom light theme by default as specified in user prompt (White cards on warm bg)
  dynamicColor: Boolean = false, // Disable system dynamic color overrides to preserve our brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

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
    primary = Emerald80,
    secondary = EmeraldGrey80,
    tertiary = Gold80,
    background = Color(0xFF141816),
    surface = Color(0xFF1B221E)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Emerald40,
    secondary = EmeraldGrey40,
    tertiary = Gold40,
    background = BackgroundSand,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default so our handcrafted Islamic theme is consistently shown
  dynamicColor: Boolean = false,
  fontName: String = "Thamanyah",
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val dynamicTypography = getTypography(getFontFamily(fontName))

  MaterialTheme(
    colorScheme = colorScheme,
    typography = dynamicTypography,
    content = content
  )
}

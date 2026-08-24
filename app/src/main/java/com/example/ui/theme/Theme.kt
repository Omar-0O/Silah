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

// ─────────────────────────────────────────────────────────────────────────────
// Sila Design System — Material 3 Color Schemes
// Refined for world-class aesthetics: warm, earthy, trustworthy.
// ─────────────────────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary              = PrimaryGreenLight,        // #4DB882
    onPrimary            = Color(0xFF041B0E),
    primaryContainer     = Color(0xFF143B28),
    onPrimaryContainer   = Color(0xFFB5EAD0),

    secondary            = SageLight,                // #8DB39E
    onSecondary          = Color(0xFF081B10),
    secondaryContainer   = Color(0xFF1B382B),
    onSecondaryContainer = Color(0xFFC7E6D6),

    tertiary             = GoldGlow,                 // #F5D278
    onTertiary           = Color(0xFF2A1C00),
    tertiaryContainer    = Color(0xFF402E05),
    onTertiaryContainer  = Color(0xFFF7DE98),

    background           = DeepCharcoal,             // #0C1410
    onBackground         = Color(0xFFF0F7F2),

    surface              = SurfaceDark,              // #121D16
    onSurface            = Color(0xFFF0F7F2),

    surfaceVariant       = SurfaceDarkVariant,       // #1A2921
    onSurfaceVariant     = Color(0xFFA5CBB4),

    outline              = Color(0xFF284032),
    outlineVariant       = Color(0xFF1C3024),

    error                = Color(0xFFFF6B6B),
    onError              = Color(0xFF380000),
    errorContainer       = Color(0xFF4A1818),
    onErrorContainer     = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
    primary              = PrimaryGreen,             // #16503A
    onPrimary            = Color.White,
    primaryContainer     = CardLightGreen,           // #F0F6F2
    onPrimaryContainer   = Color(0xFF082518),

    secondary            = SecondaryGreyGreen,       // #3E6B52
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFE8F3ED),
    onSecondaryContainer = Color(0xFF1A3A28),

    tertiary             = SoftGoldDark,             // #C09A30
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFFFF3D6),
    onTertiaryContainer  = Color(0xFF2A1C00),

    background           = BackgroundSand,           // #F7F4EF — warm linen
    onBackground         = TextDark,                 // #0F1E15

    surface              = SurfaceLight,             // #FFFFFF
    onSurface            = TextDark,

    surfaceVariant       = SurfaceLightElevated,     // #F2EFE9
    onSurfaceVariant     = Color(0xFF3A5E48),

    outline              = Color(0xFFD8E8DC),
    outlineVariant       = Color(0xFFEAF1EC),

    error                = AlertRed,
    onError              = Color.White,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    fontName: String = "Almarai",
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val dynamicTypography = getTypography(getFontFamily(fontName))

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}

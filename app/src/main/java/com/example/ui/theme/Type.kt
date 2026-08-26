package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.em
import com.example.R

// ─────────────────────────────────────────────────────────────────────────────
// Google Font Provider
// ─────────────────────────────────────────────────────────────────────────────
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val AlmaraiLocalFontFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.almarai_light, weight = FontWeight.Light),
    androidx.compose.ui.text.font.Font(R.font.almarai_regular, weight = FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.almarai_bold, weight = FontWeight.Bold),
    androidx.compose.ui.text.font.Font(R.font.almarai_extrabold, weight = FontWeight.ExtraBold)
)

// ─────────────────────────────────────────────────────────────────────────────
// Dynamic font family loader
// ─────────────────────────────────────────────────────────────────────────────
fun getFontFamily(fontName: String = "Almarai"): FontFamily {
    if (fontName == "Almarai") return AlmaraiLocalFontFamily

    val googleFontName = when (fontName) {
        "Cairo"      -> "Cairo"
        "Readex Pro" -> "Readex Pro"
        "Tajawal"    -> "Tajawal"
        "Lemonada"   -> "Lemonada"
        else         -> "Almarai"
    }
    val font = GoogleFont(googleFontName)
    return FontFamily(
        Font(googleFont = font, fontProvider = fontProvider, weight = FontWeight.Light),
        Font(googleFont = font, fontProvider = fontProvider, weight = FontWeight.Normal),
        Font(googleFont = font, fontProvider = fontProvider, weight = FontWeight.Medium),
        Font(googleFont = font, fontProvider = fontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = font, fontProvider = fontProvider, weight = FontWeight.Bold),
        Font(googleFont = font, fontProvider = fontProvider, weight = FontWeight.ExtraBold)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Typography — tuned for Arabic & English readability
// Based on Material 3 type scale + micro-adjustments for warmth & elegance
// ─────────────────────────────────────────────────────────────────────────────
fun getTypography(fontFamily: FontFamily): Typography {
    return Typography(
        // Large display — hero text
        displayLarge = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Light,
            fontSize     = 54.sp,
            lineHeight   = 66.sp,
            letterSpacing = (-0.02).em
        ),
        displayMedium = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Light,
            fontSize     = 42.sp,
            lineHeight   = 52.sp,
            letterSpacing = (-0.01).em
        ),
        displaySmall = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = 34.sp,
            lineHeight   = 44.sp
        ),

        // Headlines — section titles
        headlineLarge = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 30.sp,
            lineHeight   = 38.sp,
            letterSpacing = (-0.01).em
        ),
        headlineMedium = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 26.sp,
            lineHeight   = 34.sp
        ),
        headlineSmall = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 22.sp,
            lineHeight   = 30.sp
        ),

        // Titles — card headers, dialog titles
        titleLarge = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Bold,
            fontSize     = 20.sp,
            lineHeight   = 28.sp,
            letterSpacing = (-0.005).em
        ),
        titleMedium = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 16.sp,
            lineHeight   = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = 14.sp,
            lineHeight   = 20.sp
        ),

        // Body — readable, comfortable line-height
        bodyLarge = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = 16.sp,
            lineHeight   = 26.sp    // ← extra breathing room
        ),
        bodyMedium = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = 14.sp,
            lineHeight   = 22.sp
        ),
        bodySmall = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = 12.sp,
            lineHeight   = 18.sp
        ),

        // Labels — chips, badges, captions
        labelLarge = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 14.sp,
            lineHeight   = 20.sp,
            letterSpacing = 0.003.em
        ),
        labelMedium = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = 12.sp,
            lineHeight   = 16.sp,
            letterSpacing = 0.005.em
        ),
        labelSmall = TextStyle(
            fontFamily   = fontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = 11.sp,
            lineHeight   = 15.sp,
            letterSpacing = 0.005.em
        ),
    )
}

// Default fallback
val Typography = getTypography(getFontFamily("Almarai"))

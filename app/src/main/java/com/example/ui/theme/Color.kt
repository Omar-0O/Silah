package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Sila Design System — World-class color tokens
// ─────────────────────────────────────────────────────────────────────────────

// ── Primary: Deep Forest Green ───────────────────────────────────────────────
val PrimaryGreen      = Color(0xFF16503A)   // رئيسي — أخضر غابي عميق
val PrimaryGreenLight = Color(0xFF4DB882)   // رئيسي فاتح (dark mode)
val PrimaryGreenMuted = Color(0xFF1E6347)   // رئيسي وسط

// ── Secondary: Warm Sage ──────────────────────────────────────────────────────
val SecondaryGreyGreen = Color(0xFF3E6B52)  // ثانوي — حكيمي دافئ
val SageLight          = Color(0xFF8DB39E)  // ثانوي فاتح

// ── Gold Accent ───────────────────────────────────────────────────────────────
val SoftGold     = Color(0xFFD4A843)        // ذهبي دافئ مكتمل
val SoftGoldDark = Color(0xFFC09A30)        // ذهبي داكن
val GoldGlow     = Color(0xFFF5D278)        // توهج ذهبي للأيقونات

// ── Backgrounds ───────────────────────────────────────────────────────────────
// Light mode: barely-there warm linen — أكثر دفئاً من الأبيض المجرد
val BackgroundSand        = Color(0xFFF7F4EF)  // خلفية رئيسية — كتان دافئ
val SurfaceLight          = Color(0xFFFFFFFF)  // سطح البطاقات — أبيض نقي
val SurfaceLightElevated  = Color(0xFFF2EFE9)  // سطح مرتفع قليلاً
val CardLightGreen        = Color(0xFFF0F6F2)  // خضرة خفيفة للبطاقات

// Dark mode: luxurious deep greens
val DeepCharcoal         = Color(0xFF0C1410)  // خلفية داكنة — أخضر فحمي
val SurfaceDark          = Color(0xFF121D16)  // بطاقات داكنة
val SurfaceDarkVariant   = Color(0xFF1A2921)  // حقول وحدود داكنة
val SurfaceDarkElevated  = Color(0xFF1F3128)  // مرتفع في الداكن

// ── Text ──────────────────────────────────────────────────────────────────────
val TextDark   = Color(0xFF0F1E15)  // نص رئيسي داكن
val TextMedium = Color(0xFF2E4D39)  // نص ثانوي
val TextLight  = Color(0xFF7FA890)  // نص ثلاثي

// ── Semantic / Status ─────────────────────────────────────────────────────────
val AlertRed    = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF2E7D32)
val WarnAmber   = Color(0xFFF59E0B)
val InfoBlue    = Color(0xFF1565C0)

// ── Call log ──────────────────────────────────────────────────────────────────
val CallIncomingColor = Color(0xFF2E7D32)
val CallOutgoingColor = Color(0xFF1565C0)
val CallMissedColor   = Color(0xFFC62828)

// ── M3 Palette aliases ────────────────────────────────────────────────────────
val Emerald80      = Color(0xFF4DB882)
val EmeraldGrey80  = Color(0xFF8DB39E)
val Gold80         = Color(0xFFF5D278)
val Emerald40      = Color(0xFF16503A)
val EmeraldGrey40  = Color(0xFF3E6B52)
val Gold40         = Color(0xFFC09A30)

// ── Gradients ─────────────────────────────────────────────────────────────────
val SilaHeaderBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF16503A), Color(0xFF0D3324))
)

val SilaCardBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF1A6045), Color(0xFF0E3D28))
)

val GlassBorderBrush = Brush.horizontalGradient(
    listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.08f))
)

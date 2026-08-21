package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Sila Palette
val PrimaryGreen = Color(0xFF1E5A35)       // زيتوني داكن فخم
val PrimaryGreenLight = Color(0xFF94DAB2)  // أخضر ناعم للمظهر الداكن
val SecondaryGreyGreen = Color(0xFF4C6B56) // زيتوني رمادي متوسط
val SoftGold = Color(0xFFE9CE79)           // ذهبي دافئ
val SoftGoldDark = Color(0xFFD5BE72)       // ذهبي غامق للأزرار
val BackgroundSand = Color(0xFFFAF9F5)     // لون الرمل الدافئ
val DeepCharcoal = Color(0xFF141816)       // فحمي عميق للمظهر الداكن
val SurfaceDark = Color(0xFF1B221E)        // سطح داكن مريح

val Emerald80 = Color(0xFF8BAE96)
val EmeraldGrey80 = Color(0xFFAABFBC)
val Gold80 = Color(0xFFE9CE79)

val Emerald40 = Color(0xFF3F694D)
val EmeraldGrey40 = Color(0xFF6B8775)
val Gold40 = Color(0xFFC5AB58)

val CardLightGreen = Color(0xFFECF5F0)
val TextDark = Color(0xFF1C221E)
val AlertRed = Color(0xFFD32F2F)
val SoftYellow = Color(0xFFFFF9E6)

// Call Log Type Colors
val CallIncomingColor = Color(0xFF2E7D32)  // أخضر للمكالمات الواردة
val CallOutgoingColor = Color(0xFF1565C0)  // أزرق للمكالمات الصادرة
val CallMissedColor = Color(0xFFC62828)    // أحمر للمكالمات المفقودة

// Brushes
val SilaHeaderBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF436F51), Color(0xFF2E513A))
)

val GlassBorderBrush = Brush.horizontalGradient(
    listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
)

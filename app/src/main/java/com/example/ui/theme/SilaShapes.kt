package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Sila Design System shape and geometry tokens.
 */
object SilaShapes {
    val Chip = RoundedCornerShape(8.dp)
    val Tag = RoundedCornerShape(10.dp)
    val Button = RoundedCornerShape(14.dp)
    val ActionCard = RoundedCornerShape(16.dp)
    val Card = RoundedCornerShape(20.dp)
    val CardLarge = RoundedCornerShape(24.dp)
    val Dialog = RoundedCornerShape(24.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Pill = RoundedCornerShape(50.dp)
    val Avatar = CircleShape
}

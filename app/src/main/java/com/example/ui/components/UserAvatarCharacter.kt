package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatarCharacter(
    gender: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    showBorder: Boolean = true
) {
    val isFemale = gender.equals("female", ignoreCase = true) || gender.contains("انثى") || gender.contains("أنثى")

    val bgGradientColor = if (isFemale) Color(0xFFFCE4EC) else Color(0xFFE0F2F1)
    val accentColor = if (isFemale) Color(0xFFE91E63) else Color(0xFF0E7075)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgGradientColor)
            .then(
                if (showBorder) Modifier.border(2.dp, accentColor, CircleShape) else Modifier
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Skin color
            val skinColor = Color(0xFFFFD1B3)
            // Clothes color
            val clothesColor = if (isFemale) Color(0xFFD81B60) else Color(0xFF00897B)
            // Hair / Hijab color
            val hairColor = if (isFemale) Color(0xFF4A148C) else Color(0xFF37474F)

            // 1. Shoulders / Clothes (Bottom Arc)
            drawArc(
                color = clothesColor,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.15f, h * 0.55f),
                size = Size(w * 0.7f, h * 0.7f)
            )

            // 2. Neck
            drawRect(
                color = skinColor,
                topLeft = Offset(w * 0.42f, h * 0.45f),
                size = Size(w * 0.16f, h * 0.18f)
            )

            // 3. Face Circle
            drawCircle(
                color = skinColor,
                center = Offset(w * 0.5f, h * 0.42f),
                radius = w * 0.22f
            )

            // 4. Eyes
            val eyeY = h * 0.42f
            drawCircle(
                color = Color(0xFF1E293B),
                center = Offset(w * 0.42f, eyeY),
                radius = w * 0.035f
            )
            drawCircle(
                color = Color(0xFF1E293B),
                center = Offset(w * 0.58f, eyeY),
                radius = w * 0.035f
            )

            // 5. Smile Arc
            val smilePath = Path().apply {
                moveTo(w * 0.43f, h * 0.5f)
                quadraticTo(w * 0.5f, h * 0.56f, w * 0.57f, h * 0.5f)
            }
            drawPath(
                path = smilePath,
                color = Color(0xFFC62828),
                style = Stroke(width = w * 0.03f)
            )

            // 6. Hair / Hijab / Cap based on Gender
            if (isFemale) {
                // Female Hijab / Hair Frame
                val hijabPath = Path().apply {
                    moveTo(w * 0.22f, h * 0.45f)
                    quadraticTo(w * 0.5f, h * 0.1f, w * 0.78f, h * 0.45f)
                    quadraticTo(w * 0.85f, h * 0.75f, w * 0.72f, h * 0.82f)
                    lineTo(w * 0.28f, h * 0.82f)
                    quadraticTo(w * 0.15f, h * 0.75f, w * 0.22f, h * 0.45f)
                }
                drawPath(path = hijabPath, color = hairColor)

                // Re-draw face cutout over Hijab
                drawCircle(
                    color = skinColor,
                    center = Offset(w * 0.5f, h * 0.44f),
                    radius = w * 0.19f
                )
                // Re-draw eyes
                drawCircle(color = Color(0xFF1E293B), center = Offset(w * 0.43f, h * 0.43f), radius = w * 0.03f)
                drawCircle(color = Color(0xFF1E293B), center = Offset(w * 0.57f, h * 0.43f), radius = w * 0.03f)

                // Blush cheeks
                drawCircle(color = Color(0xFFFF8A80), center = Offset(w * 0.38f, h * 0.48f), radius = w * 0.04f)
                drawCircle(color = Color(0xFFFF8A80), center = Offset(w * 0.62f, h * 0.48f), radius = w * 0.04f)

                // Re-draw Smile
                drawPath(path = smilePath, color = Color(0xFFC62828), style = Stroke(width = w * 0.035f))
            } else {
                // Male Hair Cap / Hair Top
                val hairPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.4f)
                    quadraticTo(w * 0.5f, h * 0.12f, w * 0.75f, h * 0.4f)
                    quadraticTo(w * 0.75f, h * 0.28f, w * 0.5f, h * 0.22f)
                    quadraticTo(w * 0.25f, h * 0.28f, w * 0.25f, h * 0.4f)
                }
                drawPath(path = hairPath, color = hairColor)
            }
        }
    }
}

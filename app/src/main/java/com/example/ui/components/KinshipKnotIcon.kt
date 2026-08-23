package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic Silah (صِلَةِ) Original Ladder & Kinship Emblem (الشعار السلم الأصلي).
 * Features:
 * - Emerald Green Gradient Base
 * - Soft Gold Interconnected Ladder & Kinship Knot symbol
 */
@Composable
fun KinshipKnotIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    color: Color? = null
) {
    val defaultGold = Color(0xFFE9CE79)
    val strokeColor = color ?: defaultGold

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0E7075), Color(0xFF0A4E52), Color(0xFF063336))
                )
            )
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = w * 0.1f

            // 1. Vertical Ladder Side Rails
            val leftRailX = w * 0.3f
            val rightRailX = w * 0.7f

            drawLine(
                color = strokeColor,
                start = Offset(leftRailX, h * 0.15f),
                end = Offset(leftRailX, h * 0.85f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeColor,
                start = Offset(rightRailX, h * 0.15f),
                end = Offset(rightRailX, h * 0.85f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // 2. Ladder Rungs (Steps of Kinship / الدرجات)
            val rung1Y = h * 0.32f
            val rung2Y = h * 0.5f
            val rung3Y = h * 0.68f

            drawLine(
                color = strokeColor,
                start = Offset(leftRailX, rung1Y),
                end = Offset(rightRailX, rung1Y),
                strokeWidth = strokeW * 0.9f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeColor,
                start = Offset(leftRailX, rung2Y),
                end = Offset(rightRailX, rung2Y),
                strokeWidth = strokeW * 0.9f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeColor,
                start = Offset(leftRailX, rung3Y),
                end = Offset(rightRailX, rung3Y),
                strokeWidth = strokeW * 0.9f,
                cap = StrokeCap.Round
            )

            // 3. Central Interconnecting Kinship Knot Loop
            val knotPath = Path().apply {
                moveTo(leftRailX, rung2Y)
                cubicTo(w * 0.1f, h * 0.2f, w * 0.9f, h * 0.8f, rightRailX, rung2Y)
            }
            drawPath(
                path = knotPath,
                color = strokeColor.copy(alpha = 0.85f),
                style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round)
            )
        }
    }
}

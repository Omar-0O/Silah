package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SoftGold

@Composable
fun KinshipKnotIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.5f)
            cubicTo(w * 0.25f, h * 0.2f, w * 0.45f, h * 0.2f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.55f, h * 0.8f, w * 0.75f, h * 0.8f, w * 0.75f, h * 0.5f)
            cubicTo(w * 0.75f, h * 0.2f, w * 0.55f, h * 0.2f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.45f, h * 0.8f, w * 0.25f, h * 0.8f, w * 0.25f, h * 0.5f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(color = color, radius = 2.5f.dp.toPx(), center = Offset(w * 0.25f, h * 0.5f))
        drawCircle(color = color, radius = 2.5f.dp.toPx(), center = Offset(w * 0.75f, h * 0.5f))
        drawCircle(color = SoftGold, radius = 2f.dp.toPx(), center = Offset(w * 0.5f, h * 0.5f))
    }
}

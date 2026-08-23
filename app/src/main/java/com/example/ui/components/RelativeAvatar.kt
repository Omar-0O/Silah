package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import kotlin.math.abs

private val avatarPalette = listOf(
    Pair(Color(0xFF1A5C4A), Color(0xFF2A9D6E)),
    Pair(Color(0xFF5C3B1A), Color(0xFF9D6A2A)),
    Pair(Color(0xFF1A3A5C), Color(0xFF2A6A9D)),
    Pair(Color(0xFF4A1A5C), Color(0xFF7A2A9D)),
    Pair(Color(0xFF5C1A2E), Color(0xFF9D2A4A)),
    Pair(Color(0xFF2E5C1A), Color(0xFF4A9D2A))
)

@Composable
fun RelativeAvatar(
    name: String,
    photoUri: String? = null,
    size: Dp = 48.dp,
    fontSize: TextUnit = 20.sp,
    modifier: Modifier = Modifier
) {
    val (avatarFrom, avatarTo) = avatarPalette[abs(name.hashCode()) % avatarPalette.size]

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        if (!photoUri.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = photoUri,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    FallbackInitialsAvatar(name, avatarFrom, avatarTo, fontSize)
                },
                error = {
                    FallbackInitialsAvatar(name, avatarFrom, avatarTo, fontSize)
                }
            )
        } else {
            FallbackInitialsAvatar(name, avatarFrom, avatarTo, fontSize)
        }
    }
}

@Composable
private fun FallbackInitialsAvatar(
    name: String,
    fromColor: Color,
    toColor: Color,
    fontSize: TextUnit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(fromColor, toColor)))
    ) {
        Text(
            text = name.take(1),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

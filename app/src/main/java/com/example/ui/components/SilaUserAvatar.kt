package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Renders the user's chosen DiceBear Critters SVG avatar.
 * Falls back gracefully to UserAvatarCharacter if loading or offline.
 */
@Composable
fun SilaUserAvatar(
    avatarId: String,
    size: Dp = 52.dp,
    showBorder: Boolean = true,
    modifier: Modifier = Modifier
) {
    val avatar = ALL_AVATARS.find { it.id == avatarId } ?: ALL_AVATARS.first()
    val context = LocalContext.current

    val isFemale = avatar.gender.equals("female", ignoreCase = true)
    val accentColor = if (isFemale) Color(0xFFE91E63) else Color(0xFF0E7075)

    val imageRequest = remember(avatar.diceBearUrl) {
        ImageRequest.Builder(context)
            .data(avatar.diceBearUrl)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isFemale) Color(0xFFFCE4EC) else Color(0xFFE0F2F1))
            .then(
                if (showBorder) Modifier.border(2.dp, accentColor, CircleShape) else Modifier
            )
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = avatar.label,
            modifier = Modifier.fillMaxSize(),
            loading = {
                UserAvatarCharacter(
                    gender = avatar.gender,
                    size = size,
                    showBorder = false
                )
            },
            error = {
                UserAvatarCharacter(
                    gender = avatar.gender,
                    size = size,
                    showBorder = false
                )
            }
        )
    }
}

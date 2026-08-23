package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftGold

/**
 * Renders the user's chosen avatar (emoji inside a gradient circle).
 * Falls back gracefully if the avatarId is not found.
 */
@Composable
fun SilaUserAvatar(
    avatarId: String,
    size: Dp = 52.dp,
    showBorder: Boolean = true,
    modifier: Modifier = Modifier
) {
    val avatar = ALL_AVATARS.find { it.id == avatarId } ?: ALL_AVATARS.first()

    UserAvatarCharacter(
        gender = avatar.gender,
        size = size,
        showBorder = showBorder,
        modifier = modifier
    )
}

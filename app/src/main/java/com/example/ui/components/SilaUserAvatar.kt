package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreyGreen
import java.io.File

/**
 * Renders the user's avatar.
 * If the user has uploaded a custom personal photo, it is displayed.
 * Otherwise, displays a refined monogram of the user's initial or a clean person icon
 * in the Sila design theme.
 */
@Composable
fun SilaUserAvatar(
    photoPath: String? = null,
    userName: String = "",
    size: Dp = 52.dp,
    showBorder: Boolean = true,
    timestamp: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageFile = remember(photoPath, timestamp) {
        if (!photoPath.isNullOrBlank()) {
            val f = File(photoPath)
            if (f.exists() && f.length() > 0) f else null
        } else {
            // Check legacy custom_avatar.jpg
            val legacy = File(context.filesDir, "custom_avatar.jpg")
            if (legacy.exists() && legacy.length() > 0) legacy else null
        }
    }

    val accentColor = PrimaryGreen

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showBorder) Modifier.border(2.dp, accentColor, CircleShape) else Modifier
            )
    ) {
        if (imageFile != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageFile)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = "User Personal Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size * 0.4f),
                            strokeWidth = 2.dp,
                            color = PrimaryGreen
                        )
                    }
                },
                error = {
                    UserMonogramFallback(userName = userName, size = size)
                }
            )
        } else {
            UserMonogramFallback(userName = userName, size = size)
        }
    }
}

/**
 * Backward compatibility overload for existing callers passing avatarId.
 */
@Composable
fun SilaUserAvatar(
    avatarId: String,
    size: Dp = 52.dp,
    showBorder: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customFile = remember(avatarId) {
        val f = File(context.filesDir, "custom_avatar.jpg")
        if (f.exists() && f.length() > 0) f.absolutePath else null
    }
    SilaUserAvatar(
        photoPath = customFile,
        userName = "",
        size = size,
        showBorder = showBorder,
        modifier = modifier
    )
}

@Composable
private fun UserMonogramFallback(
    userName: String,
    size: Dp
) {
    val initial = userName.trim().firstOrNull()?.toString()?.uppercase() ?: ""
    val gradient = Brush.linearGradient(
        listOf(PrimaryGreen, SecondaryGreyGreen)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        if (initial.isNotBlank() && initial != "👤") {
            Text(
                text = initial,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

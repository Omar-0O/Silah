package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun KinshipKnotIcon(
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "شعار التطبيق — صِلَةِ",
        colorFilter = color?.let { ColorFilter.tint(it) },
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
    )
}


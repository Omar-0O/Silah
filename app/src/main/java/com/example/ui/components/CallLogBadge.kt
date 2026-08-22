package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallIncomingColor
import com.example.ui.theme.CallMissedColor
import com.example.ui.theme.CallOutgoingColor

@Composable
fun CallLogBadge(
    logType: String,
    modifier: Modifier = Modifier
) {
    val isIncoming = logType.contains("واردة", ignoreCase = true)
    val isOutgoing = logType.contains("صادرة", ignoreCase = true)
    val isMissed = logType.contains("مفقودة", ignoreCase = true)

    val (bgColor, textColor, label, icon) = when {
        isIncoming -> Quadruple(
            CallIncomingColor.copy(alpha = 0.12f),
            CallIncomingColor,
            "مكالمة واردة 📲",
            Icons.AutoMirrored.Filled.CallReceived
        )
        isOutgoing -> Quadruple(
            CallOutgoingColor.copy(alpha = 0.12f),
            CallOutgoingColor,
            "مكالمة صادرة 📞",
            Icons.AutoMirrored.Filled.CallMade
        )
        isMissed -> Quadruple(
            CallMissedColor.copy(alpha = 0.12f),
            CallMissedColor,
            "مكالمة مفقودة ❌",
            Icons.AutoMirrored.Filled.PhoneMissed
        )
        else -> Quadruple(
            Color(0xFFE9CE79).copy(alpha = 0.2f),
            Color(0xFF8B6B00),
            logType,
            Icons.AutoMirrored.Filled.PhoneCallback
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

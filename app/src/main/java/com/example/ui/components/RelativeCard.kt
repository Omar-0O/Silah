package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel

@Composable
fun RelativeCard(
    relative: Relative,
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val status = viewModel.getRelativeStatus(relative)
    val lang by viewModel.selectedLanguage.collectAsState()
    val statusColor = status.color

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onCardClick?.invoke() ?: run { viewModel.selectRelativeForDetail(relative) }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            RelativeAvatar(
                name = relative.name,
                photoUri = relative.photoUri,
                size = 52.dp,
                fontSize = 22.sp
            )

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = relative.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = DateUtils.translateDegree(relative.relationshipDegree, lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Soft Status & Last Contact Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = statusEmoji(status),
                                fontSize = 11.sp
                            )
                            Text(
                                text = status.getLabel(lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }

                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )

                    Text(
                        text = if (relative.lastContactDate == 0L) {
                            if (lang == "en") "No contact yet" else "لم تتواصل بعد"
                        } else {
                            DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Subtle forward navigation indicator
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun statusEmoji(status: RelativeStatus) = when (status) {
    RelativeStatus.CONNECTED            -> "✅"
    RelativeStatus.OK_SOON              -> "🕐"
    RelativeStatus.NEEDS_CONTACT        -> "🔔"
    RelativeStatus.OVERDUE_CRITICAL     -> "❤️"
    RelativeStatus.NEEDS_CONTACT_URGENT -> "🌿"
}

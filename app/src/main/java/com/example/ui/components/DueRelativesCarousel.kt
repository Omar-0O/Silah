package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Relative
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeViewModel
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import com.example.ui.theme.SilaShapes
import com.example.ui.theme.SoftGoldDark
import com.example.ui.theme.spacing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

private enum class CarouselDisplayState {
    EMPTY,
    ALL_CONNECTED,
    LIST
}

@Composable
fun DueRelativesCarousel(
    dueRelatives: List<Relative>,
    viewModel: RelativeViewModel,
    totalRelativesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lang by viewModel.selectedLanguage.collectAsState()
    val spacing = MaterialTheme.spacing

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.padding(bottom = spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            totalRelativesCount == 0 -> SoftGold
                            dueRelatives.isNotEmpty() -> Color(0xFFE57373)
                            else -> Color(0xFF81C784)
                        }
                    )
            )
            Text(
                text = if (totalRelativesCount == 0) stringResource(R.string.start_kinship_connection)
                       else stringResource(R.string.due_to_connect),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (totalRelativesCount > 0 && dueRelatives.isNotEmpty()) {
                Badge(
                    containerColor = Color(0xFFE57373).copy(alpha = 0.2f),
                    contentColor = Color(0xFFD32F2F),
                    modifier = Modifier.padding(start = spacing.xs)
                ) {
                    Text(" ${dueRelatives.size} ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        val carouselState = when {
            totalRelativesCount == 0 -> CarouselDisplayState.EMPTY
            dueRelatives.isEmpty() -> CarouselDisplayState.ALL_CONNECTED
            else -> CarouselDisplayState.LIST
        }

        AnimatedContent(
            targetState = carouselState,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "due_carousel_transition"
        ) { displayState ->
            when (displayState) {
                CarouselDisplayState.EMPTY -> {
                    Card(
                        shape = SilaShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                viewModel.showAddRelativeDialog.value = true
                            }
                            .border(1.dp, SoftGold.copy(alpha = 0.35f), SilaShapes.Card)
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = SoftGold,
                                modifier = Modifier.size(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.no_relatives_home_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.no_relatives_home_desc),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                CarouselDisplayState.ALL_CONNECTED -> {
                    Card(
                        shape = SilaShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), SilaShapes.Card)
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.all_connected_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.all_connected_desc),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                CarouselDisplayState.LIST -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = spacing.sm, horizontal = spacing.xxs)
                    ) {
                        items(dueRelatives, key = { it.id }) { relative ->
                        val status = viewModel.getRelativeStatus(relative)
                        val statusBgColor = status.color.copy(alpha = 0.15f)
                        val statusTextColor = status.color

                        Card(
                            modifier = Modifier
                                .width(285.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = SilaShapes.Card,
                                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                )
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    viewModel.showLogsHistoryDialog.value = relative
                                },
                            shape = SilaShapes.Card,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                // 1. Status & Degree Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(SilaShapes.Chip)
                                            .background(statusBgColor)
                                            .padding(horizontal = spacing.sm, vertical = spacing.xs)
                                    ) {
                                        Text(
                                            text = if (lang == "en") status.labelEn else status.label,
                                            color = statusTextColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = DateUtils.translateDegree(relative.relationshipDegree, lang),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(spacing.md))

                                // 2. Relative Avatar, Name & Last Contact Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RelativeAvatar(
                                        name = relative.name,
                                        photoUri = relative.photoUri,
                                        size = 46.dp,
                                        fontSize = 18.sp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = relative.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.last_contact_prefix, DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(spacing.md))

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )

                                Spacer(modifier = Modifier.height(spacing.md))

                                // 3. Action Buttons Row (Call, WhatsApp, Log)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Call Action
                                    Surface(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${relative.phone}")
                                                })
                                            } catch (e: Exception) {
                                                Toast.makeText(context, context.getString(R.string.dialer_error), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = SilaShapes.Button,
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Call,
                                                contentDescription = stringResource(R.string.action_call),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.action_call),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // WhatsApp Action
                                    Surface(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            try {
                                                var cleanPhone = relative.phone.replace("""[\s\-\(\)]""".toRegex(), "")
                                                val formattedPhone = when {
                                                    cleanPhone.startsWith("+") -> cleanPhone.substring(1)
                                                    cleanPhone.startsWith("00") -> cleanPhone.substring(2)
                                                    cleanPhone.startsWith("01") && cleanPhone.length == 11 -> "20" + cleanPhone.substring(1)
                                                    cleanPhone.startsWith("05") && cleanPhone.length == 10 -> "966" + cleanPhone.substring(1)
                                                    cleanPhone.startsWith("0") -> cleanPhone.substring(1)
                                                    else -> cleanPhone
                                                }
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    data = "https://api.whatsapp.com/send?phone=$formattedPhone".toUri()
                                                }
                                                context.startActivity(intent)
                                                val commType = context.getString(R.string.message)
                                                val note = context.getString(R.string.comm_whatsapp_quick)
                                                viewModel.recordCommunication(relative.id, commType, note)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, context.getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = SilaShapes.Button,
                                        color = Color(0xFF1B8A4A),
                                        contentColor = Color.White,
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                                contentDescription = stringResource(R.string.action_whatsapp),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.action_whatsapp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Log Record Action (WCAG AA Compliant contrast)
                                    Surface(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            viewModel.showRecordLogDialog.value = relative
                                        },
                                        shape = SilaShapes.Button,
                                        color = SoftGold.copy(alpha = 0.22f),
                                        contentColor = SoftGoldDark,
                                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.35f)),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = stringResource(R.string.action_log),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.action_log),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

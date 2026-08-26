package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel
import kotlin.math.abs

// ─────────────────────────────────────────────────
// Pastel avatar background palette (rotates by name hash)
// ─────────────────────────────────────────────────
private val avatarPalette = listOf(
    Pair(Color(0xFF1A5C4A), Color(0xFF2A9D6E)), // Deep Teal
    Pair(Color(0xFF5C3B1A), Color(0xFF9D6A2A)), // Warm Amber
    Pair(Color(0xFF1A3A5C), Color(0xFF2A6A9D)), // Ocean Blue
    Pair(Color(0xFF4A1A5C), Color(0xFF7A2A9D)), // Royal Purple
    Pair(Color(0xFF5C1A2E), Color(0xFF9D2A4A)), // Rose
    Pair(Color(0xFF2E5C1A), Color(0xFF4A9D2A)), // Leaf Green
)

private fun avatarColors(name: String): Pair<Color, Color> =
    avatarPalette[abs(name.hashCode()) % avatarPalette.size]

@Composable
fun RelativeCard(
    relative: Relative,
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val status = viewModel.getRelativeStatus(relative)
    val lang by viewModel.selectedLanguage.collectAsState()

    val statusColor = Color(android.graphics.Color.parseColor("#FF" + status.colorHex))
    val countdownText = buildCountdownText(relative, lang)

    val (avatarFrom, avatarTo) = avatarColors(relative.name)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    // ── Card Shell ───────────────────────────────────────────────────────────
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
            .clickable { onCardClick?.invoke() ?: run { viewModel.showLogsHistoryDialog.value = relative } },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {

            // ── Top Content ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Avatar circle with gradient + initials
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(avatarFrom, avatarTo))
                        )
                ) {
                    Text(
                        text = relative.name.take(1),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Name + degree + status row
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
                        text = relative.relationshipDegree,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status pill
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = statusColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = statusEmoji(status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 16.sp
                    )
                }
            }

            // ── Countdown bar ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = countdownText,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = if (lang == "en") "/${relative.contactIntervalDays}d"
                               else "/${relative.contactIntervalDays}ي",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Last contact line
            Text(
                text = if (lang == "en")
                    "Last: ${com.example.utils.DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)}"
                else
                    "آخر تواصل: ${com.example.utils.DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )

            // ── Divider ───────────────────────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )

            // ── Adaptive Action Bar ───────────────────────────────────────────
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                val cardWidth = maxWidth
                val isNarrow = cardWidth < 340.dp

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Call Button
                    ActionButton(
                        icon = Icons.Outlined.Call,
                        label = if (isNarrow) "" else (if (lang == "en") "Call" else "اتصال"),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${relative.phone}")
                            })
                        }
                    )

                    // 2. WhatsApp Button
                    ActionButton(
                        icon = Icons.Outlined.Chat,
                        label = if (isNarrow) "" else (if (lang == "en") "WhatsApp" else "واتساب"),
                        containerColor = Color(0xFF1B8A4A),
                        contentColor = Color.White,
                        onClick = {
                            var phone = relative.phone.replace("""[\s\-\(\)]""".toRegex(), "")
                            if (!phone.startsWith("+") && !phone.startsWith("00")) {
                                if (phone.startsWith("0")) phone = "966" + phone.substring(1)
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone")
                            })
                            viewModel.recordCommunication(relative.id, "رسالة", "تواصل سريع عبر الواتساب")
                        }
                    )

                    // 3. Log Contact Button (First-class Action)
                    ActionButton(
                        icon = Icons.Outlined.CheckCircle,
                        label = if (isNarrow) "" else (if (lang == "en") "Log" else "سجّل"),
                        containerColor = SoftGold,
                        contentColor = Color(0xFF141816),
                        onClick = { viewModel.showRecordLogDialog.value = relative }
                    )

                    // 4. Edit Action Chip
                    ActionIconChip(
                        icon = Icons.Outlined.Edit,
                        label = if (lang == "en") "Edit" else "تعديل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        onClick = { viewModel.showEditRelativeDialog.value = relative }
                    )

                    // 5. Delete Action Chip
                    ActionIconChip(
                        icon = Icons.Outlined.Delete,
                        label = if (lang == "en") "Delete" else "حذف",
                        tint = Color(0xFFD32F2F),
                        bgColor = Color(0xFFD32F2F).copy(alpha = 0.12f),
                        onClick = { showDeleteConfirm = true }
                    )
                }
            }
        }
    }

    // ── Delete Dialog ─────────────────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    if (lang == "en") "Delete ${relative.name}?" else "حذف ${relative.name}؟",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (lang == "en")
                        "This relative and all their communication logs will be permanently deleted. This action cannot be undone."
                    else
                        "سيتم حذف هذا القريب وكل سجلات تواصله نهائياً. هذا الإجراء لا يمكن التراجع عنه.",
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRelative(relative)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(
                        if (lang == "en") "Yes, Delete" else "نعم، احذف",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (lang == "en") "Cancel" else "إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────
// Pill-button for primary actions (Call / WhatsApp / Log)
// ─────────────────────────────────────────────────
@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(
            horizontal = if (label.isEmpty()) 10.dp else 10.dp,
            vertical = 0.dp
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(icon, contentDescription = label.ifEmpty { null }, modifier = Modifier.size(15.dp))
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Compact icon-chip for secondary actions (Edit/Delete)
// ─────────────────────────────────────────────────
@Composable
private fun ActionIconChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─────────────────────────────────────────────────
// Status → single emoji (clean, no text clutter)
// ─────────────────────────────────────────────────
private fun statusEmoji(status: RelativeStatus) = when (status) {
    RelativeStatus.CONNECTED          -> "✅"
    RelativeStatus.OK_SOON            -> "🕐"
    RelativeStatus.NEEDS_CONTACT      -> "🔔"
    RelativeStatus.OVERDUE_CRITICAL   -> "🔴"
    RelativeStatus.NEEDS_CONTACT_URGENT -> "⚠️"
}

// ─────────────────────────────────────────────────
// Countdown text builder
// ─────────────────────────────────────────────────
private fun buildCountdownText(relative: Relative, lang: String = "ar"): String {
    if (relative.lastContactDate == 0L)
        return if (lang == "en") "Never contacted — reach out now! ⚠️" else "لم يتم التواصل بعد — تواصل الآن! ⚠️"
    val diffDays = ((System.currentTimeMillis() - relative.lastContactDate) / (1000 * 60 * 60 * 24)).toInt()
    val remaining = relative.contactIntervalDays - diffDays
    return if (lang == "en") when {
        remaining > 1  -> "✅ $remaining days until next contact"
        remaining == 1 -> "🕐 Tomorrow is the day!"
        remaining == 0 -> "🔔 Today is the day to reach out"
        else           -> "🔴 ${abs(remaining)} days overdue"
    } else when {
        remaining > 1  -> "✅ باقي $remaining أيام على الموعد"
        remaining == 1 -> "🕐 غداً هو الموعد!"
        remaining == 0 -> "🔔 اليوم موعد التواصل"
        else           -> "🔴 تأخرت ${abs(remaining)} أيام"
    }
}

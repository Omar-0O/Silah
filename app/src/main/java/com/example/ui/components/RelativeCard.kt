package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel
import kotlin.math.abs


@Composable
fun RelativeCard(
    relative: Relative,
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status = viewModel.getRelativeStatus(relative)
    val statusBgColor = Color(android.graphics.Color.parseColor("#15" + status.colorHex))
    val statusTextColor = Color(android.graphics.Color.parseColor("#FF" + status.colorHex))

    // Countdown/urgency info
    val countdownText = buildCountdownText(relative)
    val countdownColor = when (status) {
        RelativeStatus.CONNECTED -> Color(0xFF2E7D32)
        RelativeStatus.OK_SOON -> Color(0xFFF59E0B)
        RelativeStatus.NEEDS_CONTACT -> Color(0xFFEF6C00)
        RelativeStatus.OVERDUE_CRITICAL, RelativeStatus.NEEDS_CONTACT_URGENT -> Color(0xFFD32F2F)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .clickable { viewModel.showLogsHistoryDialog.value = relative },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── Row 1: Avatar + Name + Status badge ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = relative.name.take(1),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = relative.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = relative.relationshipDegree,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status.label,
                            color = statusTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Countdown Text ────────────────────────────────────────────
            Text(
                text = countdownText,
                fontSize = 11.sp,
                color = countdownColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Row 2: Last contact + Action buttons ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "آخر تواصل: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "التذكير: كل ${relative.contactIntervalDays} أيام",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Call
                    CardIconBtn(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${relative.phone}")
                            })
                        },
                        bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        icon = Icons.Outlined.Call,
                        tint = MaterialTheme.colorScheme.primary,
                        description = "اتصال"
                    )

                    // WhatsApp
                    CardIconBtn(
                        onClick = {
                            var phone = relative.phone.replace("""[\s\-\(\)]""".toRegex(), "")
                            if (!phone.startsWith("+") && !phone.startsWith("00")) {
                                if (phone.startsWith("0")) phone = "966" + phone.substring(1)
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone")
                            })
                            viewModel.recordCommunication(relative.id, "رسالة", "تواصل سريع عبر الواتساب")
                        },
                        bgColor = Color(0xFFE8F5E9),
                        icon = Icons.Outlined.Chat,
                        tint = Color(0xFF2E7D32),
                        description = "واتساب"
                    )

                    // Log
                    CardIconBtn(
                        onClick = { viewModel.showRecordLogDialog.value = relative },
                        bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        icon = Icons.Outlined.CheckCircle,
                        tint = MaterialTheme.colorScheme.primary,
                        description = "سجّل"
                    )

                    // Edit
                    CardIconBtn(
                        onClick = { viewModel.showEditRelativeDialog.value = relative },
                        bgColor = SoftGold.copy(alpha = 0.12f),
                        icon = Icons.Outlined.Edit,
                        tint = Color(0xFFB45309),
                        description = "تعديل"
                    )

                    // Delete
                    CardIconBtn(
                        onClick = { showDeleteConfirm = true },
                        bgColor = Color(0xFFFFEBEE),
                        icon = Icons.Outlined.Delete,
                        tint = Color(0xFFD32F2F),
                        description = "حذف"
                    )
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف ${relative.name}؟", fontWeight = FontWeight.Bold) },
            text = {
                Text(
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
                    Text("نعم، احذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun CardIconBtn(
    onClick: () -> Unit,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    description: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .background(bgColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
    }
}

private fun buildCountdownText(relative: Relative): String {
    if (relative.lastContactDate == 0L) return "⚠️ لم يتم التواصل مطلقاً — تواصل الآن!"
    val diffDays = ((System.currentTimeMillis() - relative.lastContactDate) / (1000 * 60 * 60 * 24)).toInt()
    val remaining = relative.contactIntervalDays - diffDays
    return when {
        remaining > 1 -> "🟢 بخير — تبقى $remaining أيام على موعد الاتصال"
        remaining == 1 -> "🟡 غداً آخر موعد للاتصال!"
        remaining == 0 -> "🟠 حان موعد الاتصال اليوم"
        else -> "🔴 تأخرت ${abs(remaining)} أيام عن الموعد المحدد"
    }
}

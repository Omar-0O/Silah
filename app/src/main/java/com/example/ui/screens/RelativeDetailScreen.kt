package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommunicationLog
import com.example.data.Relative
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelativeDetailScreen(
    relative: Relative,
    viewModel: RelativeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsState()
    val allLogs by viewModel.logs.collectAsState()
    val status = viewModel.getRelativeStatus(relative)
    val statusColor = Color(android.graphics.Color.parseColor("#FF${status.colorHex}"))

    val relativeLogs = remember(allLogs, relative.id) {
        allLogs.filter { it.relativeId == relative.id }.sortedByDescending { it.timestamp }
    }

    // Urgency score 0..100
    val urgencyScore = remember(relative) {
        if (relative.lastContactDate == 0L) 100f
        else {
            val diff = (System.currentTimeMillis() - relative.lastContactDate) / 86400000.0
            ((diff / relative.contactIntervalDays) * 100).toFloat().coerceIn(0f, 100f)
        }
    }

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = urgencyScore / 100f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "urgency_arc"
    )

    val dateLocale = if (lang == "en") Locale.ENGLISH else Locale("ar")
    val dateFormat = remember(lang) { SimpleDateFormat("dd MMM yyyy  •  hh:mm a", dateLocale) }

    // Avatar gradient
    val avatarPalette = listOf(
        Pair(Color(0xFF1A5C4A), Color(0xFF2A9D6E)),
        Pair(Color(0xFF5C3B1A), Color(0xFF9D6A2A)),
        Pair(Color(0xFF1A3A5C), Color(0xFF2A6A9D)),
        Pair(Color(0xFF4A1A5C), Color(0xFF7A2A9D)),
    )
    val (avatarFrom, avatarTo) = avatarPalette[kotlin.math.abs(relative.name.hashCode()) % avatarPalette.size]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditRelativeDialog.value = relative }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "تعديل", tint = SoftGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Hero Header ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PrimaryGreen, Color(0xFF0D3324))
                            )
                        )
                        .padding(top = 56.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

                        // Avatar + urgency arc
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                            // Urgency arc
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 6.dp.toPx()
                                val sweep = animatedProgress * 270f
                                drawArc(
                                    color = Color.White.copy(alpha = 0.15f),
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = when {
                                        urgencyScore >= 100f -> Color(0xFFD32F2F)
                                        urgencyScore >= 70f  -> Color(0xFFEF6C00)
                                        urgencyScore >= 40f  -> Color(0xFFFBC02D)
                                        else                 -> Color(0xFF4DB882)
                                    },
                                    startAngle = 135f,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            // Avatar circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(avatarFrom, avatarTo)))
                            ) {
                                Text(
                                    text = relative.name.take(1),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = relative.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = relative.relationshipDegree,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status pill
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = statusColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "${statusEmoji(status)}  ${status.getLabel(lang)}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reminder Badge — matches Stitch "تذكير كل 3 أيام"
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    tint = SoftGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (lang == "en")
                                        "Reminder every ${relative.contactIntervalDays} days"
                                    else
                                        "تذكير كل ${relative.contactIntervalDays} ${if (relative.contactIntervalDays == 1) "يوم" else "أيام"}",
                                    fontSize = 11.sp,
                                    color = SoftGold,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ── Quick Actions Row ────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Call
                    DetailActionButton(
                        icon = Icons.Outlined.Call,
                        label = if (lang == "en") "Call" else "اتصال",
                        containerColor = PrimaryGreen,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${relative.phone}")
                            })
                        }
                    )
                    // WhatsApp
                    DetailActionButton(
                        icon = Icons.Outlined.Chat,
                        label = "WhatsApp",
                        containerColor = Color(0xFF1B8A4A),
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            var phone = relative.phone.replace("""[\s\-\(\)]""".toRegex(), "")
                            if (!phone.startsWith("+") && !phone.startsWith("00")) {
                                if (phone.startsWith("0")) phone = "966" + phone.substring(1)
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$phone")
                            })
                            viewModel.recordCommunication(relative.id, "رسالة", "تواصل عبر الواتساب")
                        }
                    )
                    // Log
                    DetailActionButton(
                        icon = Icons.Outlined.CheckCircle,
                        label = if (lang == "en") "Log" else "سجّل",
                        containerColor = SoftGold,
                        contentColor = Color(0xFF141816),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showRecordLogDialog.value = relative }
                    )
                }
            }

            // ── Info Card ────────────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        InfoRow(
                            icon = Icons.Outlined.Phone,
                            label = if (lang == "en") "Phone" else "الهاتف",
                            value = relative.phone
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        InfoRow(
                            icon = Icons.Outlined.Schedule,
                            label = if (lang == "en") "Reminder every" else "تذكير كل",
                            value = if (lang == "en") "${relative.contactIntervalDays} days"
                                    else "${relative.contactIntervalDays} يوم"
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        InfoRow(
                            icon = Icons.Outlined.AccessTime,
                            label = if (lang == "en") "Last contact" else "آخر تواصل",
                            value = DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)
                        )
                        if (relative.notes.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            InfoRow(
                                icon = Icons.Outlined.Notes,
                                label = if (lang == "en") "Notes" else "ملاحظات",
                                value = relative.notes
                            )
                        }
                    }
                }
            }

            // ── Timeline Header ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Timeline, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (lang == "en") "Communication Timeline" else "سجل التواصل",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (relativeLogs.isNotEmpty()) {
                        Badge(
                            containerColor = PrimaryGreen.copy(alpha = 0.15f),
                            contentColor = PrimaryGreen
                        ) {
                            Text(" ${relativeLogs.size} ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Timeline Items ───────────────────────────────────────────────
            if (relativeLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📭", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (lang == "en") "No communication recorded yet"
                                           else "لم يتم تسجيل تواصل مع هذا القريب بعد",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(relativeLogs, key = { it.id }) { log ->
                    TimelineItem(log = log, dateFormat = dateFormat, isLast = log == relativeLogs.last())
                }
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = modifier.height(44.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun TimelineItem(log: CommunicationLog, dateFormat: SimpleDateFormat, isLast: Boolean) {
    val logTypeColor = when {
        log.type.contains("اتصال") || log.type.contains("call", ignoreCase = true) -> Color(0xFF1B5E20)
        log.type.contains("رسالة") || log.type.contains("message", ignoreCase = true) -> Color(0xFF1565C0)
        log.type.contains("زيارة") || log.type.contains("visit", ignoreCase = true) -> Color(0xFF6A1B9A)
        else -> Color(0xFF4E342E)
    }
    val logEmoji = when {
        log.type.contains("اتصال") || log.type.contains("واردة") || log.type.contains("صادرة") -> "📞"
        log.type.contains("رسالة") -> "💬"
        log.type.contains("زيارة") -> "🤝"
        else -> "📝"
    }

    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = if (isLast) 0.dp else 4.dp)
    ) {
        // Timeline line + dot
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(logTypeColor.copy(alpha = 0.1f))
            ) {
                Text(logEmoji, fontSize = 16.sp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Log content card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = logTypeColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = log.type,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = logTypeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = DateUtils.formatRelativeTimeExact(log.timestamp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (log.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = log.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 17.sp
                    )
                }
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun statusEmoji(status: RelativeStatus) = when (status) {
    RelativeStatus.CONNECTED            -> "✅"
    RelativeStatus.OK_SOON              -> "🕐"
    RelativeStatus.NEEDS_CONTACT        -> "🔔"
    RelativeStatus.OVERDUE_CRITICAL     -> "🔴"
    RelativeStatus.NEEDS_CONTACT_URGENT -> "⚠️"
}

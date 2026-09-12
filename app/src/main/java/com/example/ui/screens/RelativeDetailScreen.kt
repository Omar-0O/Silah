package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Notes
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommunicationLog
import com.example.data.Relative
import com.example.ui.components.RelativeAvatar
import com.example.ui.components.ReminderIntervalSelector
import com.example.ui.dialogs.AddEditRelativeDialog
import com.example.ui.dialogs.RecordLogBottomSheet
import com.example.ui.theme.*
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
    // Intercept system back gesture
    BackHandler { onBack() }

    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val lang by viewModel.selectedLanguage.collectAsState()
    val allLogs by viewModel.logs.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    val currentRelative = relatives.find { it.id == relative.id } ?: relative
    val status = viewModel.getRelativeStatus(currentRelative)
    val statusColor = status.color

    var showIntervalSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val showEditRelativeDialog by viewModel.showEditRelativeDialog.collectAsState()
    val showRecordLogDialog by viewModel.showRecordLogDialog.collectAsState()

    val relativeLogs = remember(allLogs, currentRelative.id) {
        allLogs.filter { it.relativeId == currentRelative.id }.sortedByDescending { it.timestamp }
    }

    val dateLocale = if (lang == "en") Locale.ENGLISH else Locale.forLanguageTag("ar")
    val dateFormat = remember(lang) { SimpleDateFormat("dd MMM yyyy  •  hh:mm a", dateLocale) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // ── Clean Integrated Top Bar ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onBack()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (lang == "en") "Back" else "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Title
                Text(
                    text = if (lang == "en") "Relative Profile" else "الملف الشخصي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )

                // Top Actions: Edit & Delete
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Edit Button
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            viewModel.showEditRelativeDialog.value = currentRelative
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = if (lang == "en") "Edit" else "تعديل",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Delete Button
                    Surface(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            showDeleteConfirmDialog = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = AlertRed.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.20f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = if (lang == "en") "Delete" else "حذف",
                                tint = AlertRed,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ── Soothing Hero Identity ────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(top = 10.dp, bottom = 22.dp, start = 20.dp, end = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar with peaceful status halo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(104.dp)
                        ) {
                            // Soft status ring
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(2.5.dp, statusColor.copy(alpha = 0.45f)),
                                modifier = Modifier.size(104.dp)
                            ) {}

                            // Central Avatar
                            RelativeAvatar(
                                name = currentRelative.name,
                                photoUri = currentRelative.photoUri,
                                size = 92.dp,
                                fontSize = 34.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Relative Name
                        Text(
                            text = currentRelative.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Relationship Degree
                        Text(
                            text = DateUtils.translateDegree(currentRelative.relationshipDegree, lang),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Unified Soft Status & Reminder Pill (Interactive)
                        Surface(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                showIntervalSheet = true
                            },
                            shape = RoundedCornerShape(50.dp),
                            color = statusColor.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.28f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(statusEmoji(status), fontSize = 12.sp)
                                Text(
                                    text = status.getLabel(lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                                Text(
                                    text = "•",
                                    fontSize = 11.sp,
                                    color = statusColor.copy(alpha = 0.45f)
                                )
                                Icon(
                                    Icons.Outlined.NotificationsActive,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (lang == "en")
                                        "Every ${currentRelative.contactIntervalDays}d"
                                    else
                                        "تذكير كل ${currentRelative.contactIntervalDays} ${if (currentRelative.contactIntervalDays <= 10) "أيام" else "يوم"}",
                                    fontSize = 11.sp,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = statusColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Soft Ergonomic Action Deck ────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Call Action
                    SoftActionButton(
                        icon = Icons.Outlined.Call,
                        label = if (lang == "en") "Call" else "اتصال",
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${currentRelative.phone}")
                                })
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    if (lang == "en") "Unable to open dialer" else "تعذر فتح لوحة الاتصال",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )

                    // 2. WhatsApp Action
                    SoftActionButton(
                        icon = Icons.AutoMirrored.Outlined.Chat,
                        label = if (lang == "en") "WhatsApp" else "واتساب",
                        containerColor = Color(0xFF25D366).copy(alpha = 0.12f),
                        contentColor = Color(0xFF1E8E49),
                        borderColor = Color(0xFF25D366).copy(alpha = 0.28f),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            try {
                                val cleanPhone = currentRelative.phone.replace("""[\s\-\(\)]""".toRegex(), "")
                                val formattedPhone = when {
                                    cleanPhone.startsWith("+") -> cleanPhone.substring(1)
                                    cleanPhone.startsWith("00") -> cleanPhone.substring(2)
                                    cleanPhone.startsWith("01") && cleanPhone.length == 11 -> "20" + cleanPhone.substring(1)
                                    cleanPhone.startsWith("05") && cleanPhone.length == 10 -> "966" + cleanPhone.substring(1)
                                    cleanPhone.startsWith("0") -> cleanPhone.substring(1)
                                    else -> cleanPhone
                                }
                                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone")
                                })
                                viewModel.recordCommunication(currentRelative.id, "رسالة", "تواصل عبر الواتساب")
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    if (lang == "en") "WhatsApp not installed" else "تطبيق الواتساب غير مثبت على الجهاز",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )

                    // 3. Log Interaction Action
                    SoftActionButton(
                        icon = Icons.Outlined.CheckCircle,
                        label = if (lang == "en") "Log" else "سجّل صلة",
                        containerColor = SoftGold.copy(alpha = 0.18f),
                        contentColor = SoftGoldDark,
                        borderColor = SoftGold.copy(alpha = 0.35f),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.showRecordLogDialog.value = currentRelative
                        }
                    )
                }
            }

            // ── Kinship Pulse / Quick Insights ────────────────────────────
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Metric 1: Last Contact
                        KinshipMetricItem(
                            icon = Icons.Outlined.History,
                            value = DateUtils.formatRelativeTimeExact(currentRelative.lastContactDate, lang),
                            label = if (lang == "en") "Last Contact" else "آخر تواصل",
                            modifier = Modifier.weight(1f)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        )

                        // Metric 2: Total Interactions
                        KinshipMetricItem(
                            icon = Icons.Outlined.Forum,
                            value = if (lang == "en") "${relativeLogs.size}" else "${relativeLogs.size} صلة",
                            label = if (lang == "en") "Total Logs" else "سجل الوصل",
                            modifier = Modifier.weight(1f)
                        )

                        VerticalDivider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        )

                        // Metric 3: Target Frequency
                        KinshipMetricItem(
                            icon = Icons.Outlined.Schedule,
                            value = if (lang == "en") "${currentRelative.contactIntervalDays}d" else "${currentRelative.contactIntervalDays} يوم",
                            label = if (lang == "en") "Cadence" else "فترة الوصل",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Contact Information & Notes ───────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Phone Number with Copy & Quick Dial
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (lang == "en") "Phone Number" else "رقم الهاتف",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = currentRelative.phone,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Copy button
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    clipboardManager.setText(AnnotatedString(currentRelative.phone))
                                    Toast.makeText(
                                        context,
                                        if (lang == "en") "Phone copied to clipboard 📋" else "تم نسخ الرقم إلى الحافظة 📋",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = if (lang == "en") "Copy" else "نسخ",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Notes section (if provided)
                        if (currentRelative.notes.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(SoftGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.Notes,
                                        contentDescription = null,
                                        tint = SoftGoldDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (lang == "en") "Notes & Preferences" else "ملاحظات وتفضيلات",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = currentRelative.notes,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Timeline Section Header ───────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = if (lang == "en") "Communication History" else "سجل صلة الرحم",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (relativeLogs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = if (lang == "en") "${relativeLogs.size} logs" else "${relativeLogs.size} تواصل",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── Timeline List or Warm Empty State ─────────────────────────
            if (relativeLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🌿", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (lang == "en")
                                    "No communication logged yet"
                                else
                                    "لم تسجل أي صلة مع ${currentRelative.name} بعد",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "en")
                                    "Reach out through the quick actions above and record the blessing."
                                else
                                    "بادر بالسؤال عنه عبر الأزرار السريعة بالأعلى واكسب أجر صلة الرحم ✨",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(relativeLogs, key = { log -> "${log.id}_${log.timestamp}" }) { log ->
                    TimelineItem(
                        log = log,
                        dateFormat = dateFormat,
                        lang = lang,
                        isLast = log == relativeLogs.last()
                    )
                }
            }
        }
    }

    // ── Delete Confirmation Dialog ───────────────────────────────────
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = if (lang == "en") "Delete ${currentRelative.name}?" else "حذف ${currentRelative.name}؟",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lang == "en")
                        "This relative and all their communication logs will be permanently deleted. This action cannot be undone."
                    else
                        "سيتم حذف هذا القريب وكل سجلات تواصله نهائياً. هذا الإجراء لا يمكن التراجع عنه.",
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.deleteRelative(currentRelative)
                        showDeleteConfirmDialog = false
                        Toast.makeText(
                            context,
                            if (lang == "en") "${currentRelative.name} deleted" else "تم حذف ${currentRelative.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (lang == "en") "Yes, Delete" else "نعم، احذف",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (lang == "en") "Cancel" else "إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Reminder Interval Customization Bottom Sheet ─────────────────
    if (showIntervalSheet) {
        var tempInterval by remember(currentRelative.contactIntervalDays) {
            mutableIntStateOf(currentRelative.contactIntervalDays)
        }
        ModalBottomSheet(
            onDismissRequest = { showIntervalSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = if (lang == "en") "Custom Reminder Cadence ⏰" else "تخصيص فترة التذكير ⏰",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "en")
                            "Set how often you want to be reminded to connect with ${currentRelative.name}"
                        else
                            "حدد دورية التذكير الأنسب لك للتواصل مع ${currentRelative.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                ReminderIntervalSelector(
                    intervalDays = tempInterval,
                    onIntervalChange = { tempInterval = it },
                    relationshipDegree = currentRelative.relationshipDegree,
                    lang = lang
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            showIntervalSheet = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (lang == "en") "Cancel" else "إلغاء", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.updateRelativeInterval(currentRelative, tempInterval)
                            Toast.makeText(
                                context,
                                if (lang == "en") "Reminder frequency updated ✨" else "تم تحديث موعد التذكير بنجاح ✨",
                                Toast.LENGTH_SHORT
                            ).show()
                            showIntervalSheet = false
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (lang == "en") "Save Changes" else "حفظ التعديل", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Edit Relative Dialog ─────────────────────────────────────────
    val editTarget = showEditRelativeDialog
    if (editTarget != null) {
        AddEditRelativeDialog(
            viewModel = viewModel,
            relativeToEdit = editTarget,
            onDismiss = { viewModel.showEditRelativeDialog.value = null }
        )
    }

    // ── Record Log Bottom Sheet ──────────────────────────────────────
    val logTarget = showRecordLogDialog
    if (logTarget != null) {
        RecordLogBottomSheet(
            relative = logTarget,
            viewModel = viewModel,
            onDismiss = { viewModel.showRecordLogDialog.value = null }
        )
    }
}

// ── Soft Ergonomic Action Button ──────────────────────────────────────────
@Composable
private fun SoftActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Surface(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Kinship Metric Item ───────────────────────────────────────────────────
@Composable
private fun KinshipMetricItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// ── Timeline Item with Soft Continuous Line ───────────────────────────────
@Composable
private fun TimelineItem(
    log: CommunicationLog,
    dateFormat: SimpleDateFormat,
    lang: String,
    isLast: Boolean
) {
    val logTypeColor = when {
        log.type.contains("اتصال") || log.type.contains("call", ignoreCase = true) -> Color(0xFF1B5E20)
        log.type.contains("رسالة") || log.type.contains("message", ignoreCase = true) -> Color(0xFF1565C0)
        log.type.contains("زيارة") || log.type.contains("visit", ignoreCase = true) -> Color(0xFF6A1B9A)
        else -> Color(0xFF5D4037)
    }
    val logEmoji = when {
        log.type.contains("اتصال") || log.type.contains("واردة") || log.type.contains("صادرة") -> "📞"
        log.type.contains("رسالة") -> "💬"
        log.type.contains("زيارة") -> "🤝"
        else -> "📝"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp)
    ) {
        // Step indicator: Icon circle + vertical connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(logTypeColor.copy(alpha = 0.12f))
            ) {
                Text(logEmoji, fontSize = 14.sp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Log Content Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = logTypeColor.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = logTypeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = log.type,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = logTypeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = DateUtils.formatRelativeTimeExact(log.timestamp, lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (log.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = log.notes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 6.dp)
                )
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

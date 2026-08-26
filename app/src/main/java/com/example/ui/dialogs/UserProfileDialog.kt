package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.AvatarPickerSheet
import com.example.ui.components.SilaUserAvatar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel
import java.util.Calendar

@Composable
fun UserProfileDialog(
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val userName: String by viewModel.userName.collectAsState(initial = "")
    val userAvatarId: String by viewModel.userAvatarId.collectAsState(initial = "avatar_01")
    val lang: String by viewModel.selectedLanguage.collectAsState(initial = "ar")
    val logs by viewModel.logs.collectAsState(initial = emptyList())

    var showAvatarPicker by remember { mutableStateOf(false) }

    val displayName = if (userName.isNotBlank()) userName else if (lang == "en") "there" else "عمر"

    // Calendar & Log Calculations for Current Month
    val currentCalendar = remember { Calendar.getInstance() }
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val todayDayOfMonth = currentCalendar.get(Calendar.DAY_OF_MONTH)
    val daysInCurrentMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Set of days in current month where at least one communication log exists
    val connectedDaysSet = remember(logs, currentYear, currentMonth) {
        val cal = Calendar.getInstance()
        logs.mapNotNull { log ->
            cal.timeInMillis = log.timestamp
            if (cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth) {
                cal.get(Calendar.DAY_OF_MONTH)
            } else null
        }.toSet()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // ── 1. Top Header Row ──────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right side: Avatar + User Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar with tap-to-change badge
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.clickable { showAvatarPicker = true }
                        ) {
                            SilaUserAvatar(
                                avatarId = userAvatarId,
                                size = 58.dp,
                                showBorder = true
                            )
                            // Edit badge
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(SoftGold)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = if (lang == "en") "Change Avatar" else "تغيير الصورة",
                                    tint = Color(0xFF141816),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = displayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (lang == "en") "Tap avatar to change" else "اضغط الصورة للتغيير",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Left side: Settings & Close Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.showSettingsDialog.value = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الإعدادات",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── 2. Cloud Sync Banner (أحفظ تقدمك وشارك إنجازك - قريباً) ─────────
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "سحابي",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (lang == "en") "Save Progress & Share Achievements" else "أحفظ تقدمك وشارك إنجازك",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (lang == "en") "Account creation and cloud sync coming soon" else "إنشاء الحساب ومزامنة الأصدقاء قريباً",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFFD97706),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (lang == "en") "Soon" else "قريباً",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── 3. Monthly Communication Calendar Log (سجل التواصل الشهر الحالي) ──
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (lang == "en") "Monthly Connection Log 🌸" else "سجل التواصل الشهر الحالي 🌸",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Days of Week Header
                                val weekDays = if (lang == "en")
                                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                                else
                                    listOf("أحد", "اثنين", "ثلاثاء", "اربعاء", "خميس", "جمعة", "سبت")

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    weekDays.forEach { day ->
                                        Text(
                                            text = day,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Days Grid
                                val totalRows = (daysInCurrentMonth + 6) / 7
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (week in 0 until totalRows) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            for (dayInWeek in 1..7) {
                                                val dayNum = week * 7 + dayInWeek
                                                if (dayNum <= daysInCurrentMonth) {
                                                    val isFutureDay = dayNum > todayDayOfMonth
                                                    val isConnectedDay = connectedDaysSet.contains(dayNum)

                                                    // Visual states based on user specifications:
                                                    // 1. Future Days: Dimmed / Disabled (المطفية)
                                                    // 2. Connected Days: Green (يوم صلت فيه الرحم)
                                                    // 3. Unconnected Past/Current Days: Red/Coral (يوم لا)
                                                    val bgColor = when {
                                                        isFutureDay -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                        isConnectedDay -> Color(0xFFD1FAE5)
                                                        else -> Color(0xFFFEF2F2)
                                                    }

                                                    val borderColor = when {
                                                        isFutureDay -> Color(0xFFE2E8F0).copy(alpha = 0.5f)
                                                        isConnectedDay -> Color(0xFF10B981)
                                                        else -> Color(0xFFFCA5A5)
                                                    }

                                                    val textColor = when {
                                                        isFutureDay -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                        isConnectedDay -> Color(0xFF047857)
                                                        else -> Color(0xFFDC2626)
                                                    }

                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(bgColor)
                                                            .border(
                                                                BorderStroke(
                                                                    width = if (isConnectedDay) 1.5.dp else 1.dp,
                                                                    color = borderColor
                                                                ),
                                                                CircleShape
                                                            )
                                                    ) {
                                                        Text(
                                                            text = "$dayNum",
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isConnectedDay) FontWeight.Bold else FontWeight.Medium,
                                                            color = textColor
                                                        )
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(36.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Dynamic Legend Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Connected legend
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Text(
                                            text = if (lang == "en") "Connected" else "صلت فيه الرحم",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857)
                                        )
                                    }

                                    // Unconnected legend
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                        )
                                        Text(
                                            text = if (lang == "en") "No contact" else "لم تصل فيه",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }

                                    // Future day legend
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFCBD5E1))
                                        )
                                        Text(
                                            text = if (lang == "en") "Upcoming" else "أيام قادمة",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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

    // ── Avatar Picker Sheet ───────────────────────────────────────────────────
    if (showAvatarPicker) {
        AvatarPickerSheet(
            currentAvatarId = userAvatarId,
            lang = lang,
            onAvatarSelected = { newId ->
                viewModel.saveUserAvatar(newId)
            },
            onDismiss = { showAvatarPicker = false }
        )
    }
}

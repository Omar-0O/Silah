package com.example.ui.dialogs

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Calendar
import androidx.compose.ui.draw.alpha
import com.example.data.Relative
import com.example.ui.components.AvatarPickerSheet
import com.example.ui.components.SilaUserAvatar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun UserProfileDialog(
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val userName: String by viewModel.userName.collectAsState(initial = "")
    val userGender: String by viewModel.userGender.collectAsState(initial = "male")
    val userAvatarId: String by viewModel.userAvatarId.collectAsState(initial = "avatar_01")
    val lang: String by viewModel.selectedLanguage.collectAsState(initial = "ar")
    val layoutDirection = if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
    val relativesList: List<Relative> by viewModel.relatives.collectAsState(initial = emptyList())
    val logsList: List<com.example.data.CommunicationLog> by viewModel.logs.collectAsState(initial = emptyList())

    var showAvatarPicker by remember { mutableStateOf(false) }

    val currentCal = Calendar.getInstance()
    val todayDayOfMonth = currentCal.get(Calendar.DAY_OF_MONTH)
    val maxDaysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentMonth = currentCal.get(Calendar.MONTH)
    val currentYear = currentCal.get(Calendar.YEAR)

    val firstDayOfMonthCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val startDayOfWeek = firstDayOfMonthCal.get(Calendar.DAY_OF_WEEK)
    val startOffset = startDayOfWeek - 1

    val connectedDaysThisMonth = remember(logsList, relativesList) {
        val set = mutableSetOf<Int>()
        val tempCal = Calendar.getInstance()
        logsList.forEach { log ->
            tempCal.timeInMillis = log.timestamp
            if (tempCal.get(Calendar.MONTH) == currentMonth && tempCal.get(Calendar.YEAR) == currentYear) {
                set.add(tempCal.get(Calendar.DAY_OF_MONTH))
            }
        }
        relativesList.forEach { relative ->
            relative.lastContactDate?.let { timestamp ->
                tempCal.timeInMillis = timestamp
                if (tempCal.get(Calendar.MONTH) == currentMonth && tempCal.get(Calendar.YEAR) == currentYear) {
                    set.add(tempCal.get(Calendar.DAY_OF_MONTH))
                }
            }
        }
        set
    }

    val connectedCount = relativesList.count { relative -> viewModel.getRelativeStatus(relative) == RelativeStatus.CONNECTED }
    val moderateCount = relativesList.count { relative -> viewModel.getRelativeStatus(relative) == RelativeStatus.OK_SOON || viewModel.getRelativeStatus(relative) == RelativeStatus.NEEDS_CONTACT }
    val overdueCount = relativesList.count { relative -> viewModel.getRelativeStatus(relative) == RelativeStatus.OVERDUE_CRITICAL || viewModel.getRelativeStatus(relative) == RelativeStatus.NEEDS_CONTACT_URGENT }

    val totalRelativesCount = relativesList.size
    val commitmentPercentage = if (totalRelativesCount > 0) (connectedCount * 100 / totalRelativesCount) else 100
    val displayName = if (userName.isNotBlank()) userName else if (lang == "en") "there" else "عمر"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
                // ── 1. Top Header Row (Matching user screenshot) ───────────────────
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                        Text(
                            text = displayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Left side: Settings & Close Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.showSettingsDialog.value = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = if (lang == "en") "Settings" else "الإعدادات",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (lang == "en") "Close" else "إغلاق",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── 2. General Evaluation Card (التقييم العام) ─────────────────────
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = PrimaryGreen.copy(alpha = 0.2f))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0E7075), Color(0xFF0D6367), Color(0xFF084144))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = if (lang == "en") "General Evaluation" else "التقييم العام",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Metric 1: Connects last 7 days
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (lang == "en") "Contact (Last 7 Days)" else "التواصل (آخر 7 أيام)", fontSize = 11.sp, color = Color(0xFFB2DFDB))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (lang == "en") "${totalRelativesCount * 2} contacts" else "${totalRelativesCount * 2} اتصالات", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SoftGold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .width(1.dp)
                                            .background(Color(0x33FFFFFF))
                                    )

                                    // Metric 2: Connected Relatives
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (lang == "en") "Connected" else "المتصل بهم", fontSize = 11.sp, color = Color(0xFFB2DFDB))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (lang == "en") "$connectedCount of $totalRelativesCount" else "$connectedCount من $totalRelativesCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(30.dp)
                                            .width(1.dp)
                                            .background(Color(0x33FFFFFF))
                                    )

                                    // Metric 3: Overall Commitment Percentage
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (lang == "en") "Commitment Rate" else "نسبة الالتزام", fontSize = 11.sp, color = Color(0xFFB2DFDB))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$commitmentPercentage%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SoftGold)
                                    }
                                }
                            }
                        }
                    }

                    // ── 3. Cloud Sync Banner (أحفظ تقدمك وشارك إنجازك - قريباً) ─────────
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = if (lang == "en") "Cloud" else "سحابي",
                                        tint = Color(0xFF0E7075),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (lang == "en") "Save Progress & Share Achievements" else "أحفظ تقدمك وشارك إنجازك",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF004D40)
                                        )
                                        Text(
                                            text = if (lang == "en") "Account creation & sync coming soon" else "إنشاء الحساب ومزامنة الأصدقاء قريباً",
                                            fontSize = 11.sp,
                                            color = Color(0xFF00695C)
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

                    // ── 4. Kinship Statistics & Monthly Streak (إحصائيات صلة الرحم) ────
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (lang == "en") "Kinship Statistics" else "إحصائيات صلة الرحم",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Category Icons Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون").forEach { category ->
                                        val count = relativesList.count { relative -> relative.relationshipDegree == category }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE0F2F1))
                                            ) {
                                                Text(
                                                    text = when (category) {
                                                        "والدان" -> "💖"
                                                        "أشقاء" -> "👦"
                                                        "أعمام/أخوال" -> "👴"
                                                        else -> "🤝"
                                                    },
                                                    fontSize = 20.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(com.example.utils.DateUtils.translateDegree(category, lang), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text(if (lang == "en") "$count members" else "$count أفراد", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Month Calendar Header (Matching user screenshot)
                                // Month Calendar Header
                                Text(
                                    text = if (lang == "en") "Current Month Log 🌸" else "سجل التواصل الشهر الحالي 🌸",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0E7075)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Days Header Row (Arabic / RTL order)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    val headers = if (lang == "en") {
                                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                                    } else {
                                        listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                                    }
                                    headers.forEach { day ->
                                        Text(day, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Dynamic Days Grid
                                val totalSlots = startOffset + maxDaysInMonth
                                val totalWeeks = (totalSlots + 6) / 7

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (weekIndex in 0 until totalWeeks) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            for (dayInWeek in 0 until 7) {
                                                val slotIndex = weekIndex * 7 + dayInWeek
                                                val dayNum = slotIndex - startOffset + 1

                                                if (dayNum in 1..maxDaysInMonth) {
                                                    val isConnectedDay = dayNum in connectedDaysThisMonth
                                                    val isFutureDay = dayNum > todayDayOfMonth
                                                    val isToday = dayNum == todayDayOfMonth

                                                    val bgColor = when {
                                                        isFutureDay -> Color(0xFFF1F5F9).copy(alpha = 0.5f)
                                                        isConnectedDay -> Color(0xFFE0F2F1)
                                                        else -> Color(0xFFF8FAFC)
                                                    }
                                                    val borderColor = when {
                                                        isFutureDay -> Color(0xFFE2E8F0).copy(alpha = 0.4f)
                                                        isToday -> Color(0xFFFF6D00)
                                                        isConnectedDay -> Color(0xFF0E7075)
                                                        else -> Color(0xFFE2E8F0)
                                                    }
                                                    val textColor = when {
                                                        isFutureDay -> Color(0xFFCBD5E1)
                                                        isConnectedDay -> Color(0xFF0E7075)
                                                        isToday -> Color(0xFFFF6D00)
                                                        else -> Color(0xFF64748B)
                                                    }

                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(bgColor)
                                                            .border(
                                                                width = if (isToday || isConnectedDay) 1.5.dp else 1.dp,
                                                                color = borderColor,
                                                                shape = CircleShape
                                                            )
                                                    ) {
                                                        Text(
                                                            text = "$dayNum",
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isConnectedDay || isToday) FontWeight.Bold else FontWeight.Normal,
                                                            color = textColor
                                                        )
                                                    }
                                                } else {
                                                    Box(modifier = Modifier.size(32.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Summary Ring Indicators (Bottom of User Screenshot)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (lang == "en") "Connected" else "متصل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(if (lang == "en") "$connectedCount relatives" else "$connectedCount أقارب", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (lang == "en") "Moderate" else "معتدل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(if (lang == "en") "$moderateCount relatives" else "$moderateCount أقارب", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (lang == "en") "Urgent" else "تواصل عاجل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(if (lang == "en") "$overdueCount relatives" else "$overdueCount أقارب", fontSize = 10.sp, color = Color(0xFF64748B))
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
}

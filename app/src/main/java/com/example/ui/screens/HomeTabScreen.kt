package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CommitmentHeaderCard
import com.example.ui.components.DueRelativesCarousel
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeTabScreen — الشاشة الرئيسية (Dashboard Tab)
 * تعرض CommitmentHeaderCard + StreakCard + DueRelativesCarousel + إحصائيات سريعة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(viewModel: RelativeViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatarId by viewModel.userAvatarId.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val last7DaysActivity by viewModel.last7DaysActivity.collectAsState()
    val context = LocalContext.current

    // Urgency-sorted due relatives
    val dueRelatives = remember(relatives, logs) {
        relatives.filter { r ->
            val diffDays = if (r.lastContactDate == 0L) Int.MAX_VALUE
                           else ((System.currentTimeMillis() - r.lastContactDate) / 86400000).toInt()
            diffDays >= r.contactIntervalDays
        }.sortedByDescending { r ->
            if (r.lastContactDate == 0L) Long.MAX_VALUE
            else System.currentTimeMillis() - r.lastContactDate
        }
    }

    // Stats
    val totalLogsCount = logs.size
    val uniqueDaysCount = remember(logs) {
        logs.map {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.timestamp))
        }.distinct().size
    }
    val uniqueRelativesContacted = remember(logs) {
        logs.map { it.relativeId }.distinct().size
    }

    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog) {
        com.example.ui.dialogs.UserProfileDialog(
            viewModel = viewModel,
            onDismiss = { showProfileDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        com.example.ui.components.SilaUserAvatar(
                            avatarId = userAvatarId,
                            size = 42.dp,
                            showBorder = true,
                            modifier = Modifier.clickableNoRipple { showProfileDialog = true }
                        )
                        Text(
                            text = if (userName.isNotBlank()) userName
                                   else if (lang == "en") "Family Keeper" else "حافظ الأرحام",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 17.sp
                        )
                    }
                },
                actions = {
                    // Settings
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "إعدادات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp)
        ) {

            // 1. Commitment Arc Card
            item {
                CommitmentHeaderCard(
                    totalLogsCount = totalLogsCount,
                    uniqueDaysCount = uniqueDaysCount,
                    uniqueRelativesContacted = uniqueRelativesContacted,
                    lang = lang,
                    userName = userName,
                    totalRelativesCount = relatives.size
                )
            }

            // 2. Kinship Streak Card
            item {
                StreakCard(
                    streakDays = streakDays,
                    last7DaysActivity = last7DaysActivity,
                    lang = lang
                )
            }

            // 3. Quick Stats Row
            item {
                QuickStatsRow(
                    relativesCount = relatives.size,
                    dueCount = dueRelatives.size,
                    logsCount = totalLogsCount,
                    lang = lang
                )
            }

            // 4. Due Relatives Carousel
            item {
                DueRelativesCarousel(
                    dueRelatives = dueRelatives,
                    viewModel = viewModel
                )
            }
        }
    }
}

// ── Quick Stats Row ───────────────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(
    relativesCount: Int,
    dueCount: Int,
    logsCount: Int,
    lang: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniStatCard(
            emoji = "👥",
            value = relativesCount.toString(),
            label = if (lang == "en") "Relatives" else "أقارب",
            color = PrimaryGreen,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            emoji = "🔔",
            value = dueCount.toString(),
            label = if (lang == "en") "Due Today" else "بانتظارك",
            color = if (dueCount > 0) Color(0xFFE53935) else PrimaryGreen,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            emoji = "🤝",
            value = logsCount.toString(),
            label = if (lang == "en") "Total Logs" else "صلات مسجلة",
            color = Color(0xFF0E7075),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniStatCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = modifier.shadow(2.dp, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.14f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Kinship Streak Card ────────────────────────────────────────────────────────
@Composable
private fun StreakCard(
    streakDays: Int,
    last7DaysActivity: List<Boolean>,
    lang: String
) {
    val dayLabels = remember(lang) {
        (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (lang == "en") {
                when (dayOfWeek) {
                    Calendar.SUNDAY -> "Sun"
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    else -> "Sat"
                }
            } else {
                when (dayOfWeek) {
                    Calendar.SUNDAY -> "أحد"
                    Calendar.MONDAY -> "إثنين"
                    Calendar.TUESDAY -> "ثلاثاء"
                    Calendar.WEDNESDAY -> "أربعاء"
                    Calendar.THURSDAY -> "خميس"
                    Calendar.FRIDAY -> "جمعة"
                    else -> "سبت"
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (streakDays > 0) Color(0xFFFF6D00).copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (streakDays > 0) "🔥" else "🌱",
                        fontSize = 22.sp
                    )
                }

                Column {
                    Text(
                        text = if (lang == "en") "$streakDays Day Streak!"
                               else if (streakDays == 0) "تتابع صلة الرحم 🌸"
                               else if (streakDays == 1) "يوم واحد متواصل 🔥"
                               else if (streakDays == 2) "يومان متواصلان 🔥"
                               else if (streakDays in 3..10) "$streakDays أيام متتالية 🔥"
                               else "$streakDays يوماً متتالياً 🔥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (streakDays == 0) {
                            if (lang == "en") "Connect today to start your streak!" else "تواصل اليوم لتبدأ سلسلة صلة الرحم! 🌸"
                        } else {
                            if (lang == "en") "Keep the flame of family ties glowing!" else "واصل صلة رحمك يومياً لنيل البركة والرضوان ✨"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // 7 Days Visual Row (RTL - starting from Right)
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    last7DaysActivity.forEachIndexed { index, isActive ->
                        val dayLabel = dayLabels.getOrElse(index) { "" }
                        val isToday = (index == last7DaysActivity.lastIndex)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = dayLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = if (isActive) Color(0xFFFF6D00).copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isToday) 1.5.dp else 0.dp,
                                        color = if (isToday) Color(0xFFFF6D00) else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isActive) "🔥" else "•",
                                    fontSize = if (isActive) 13.sp else 16.sp,
                                    color = if (isActive) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}

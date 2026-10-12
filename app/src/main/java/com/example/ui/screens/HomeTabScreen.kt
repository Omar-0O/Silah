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
import com.example.ui.theme.SilaShapes
import com.example.ui.theme.SoftGold
import com.example.ui.theme.spacing
import com.example.viewmodel.RelativeViewModel
import java.text.SimpleDateFormat
import java.util.*

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.ImmutableList

/**
 * HomeTabScreen — الشاشة الرئيسية (Dashboard Tab)
 * تعرض CommitmentHeaderCard + StreakCard + DueRelativesCarousel + إحصائيات سريعة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(viewModel: RelativeViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val userName by viewModel.userName.collectAsState()
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
        }.toImmutableList()
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

    val spacing = MaterialTheme.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
            contentPadding = PaddingValues(top = spacing.lg, bottom = 100.dp)
        ) {

            // 1. Commitment Arc Card
            item(key = "commitment_card") {
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
            item(key = "streak_card") {
                StreakCard(
                    streakDays = streakDays,
                    last7DaysActivity = last7DaysActivity,
                    lang = lang
                )
            }

            // 3. Due Relatives Carousel
            item(key = "due_carousel") {
                DueRelativesCarousel(
                    dueRelatives = dueRelatives,
                    viewModel = viewModel,
                    totalRelativesCount = relatives.size
                )
            }
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
    val spacing = MaterialTheme.spacing
    val dayLabels = remember(lang) {
        val locale = if (lang == "en") Locale.ENGLISH else Locale.forLanguageTag("ar")
        val dayFormat = SimpleDateFormat("EEE", locale)
        (0..6).map { offset ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            dayFormat.format(cal.time)
        }
    }

    Card(
        shape = com.example.ui.theme.SilaShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = com.example.ui.theme.SilaShapes.Card,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = if (streakDays > 0) PrimaryGreen.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (streakDays > 0) "🌿" else "🌱",
                        fontSize = 22.sp
                    )
                }

                Column {
                    Text(
                        text = androidx.compose.ui.res.pluralStringResource(
                            com.example.R.plurals.streak_days_count,
                            streakDays,
                            streakDays
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (streakDays == 0) {
                            androidx.compose.ui.res.stringResource(com.example.R.string.streak_zero_desc)
                        } else {
                            androidx.compose.ui.res.stringResource(com.example.R.string.streak_active_desc)
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 7 Days Visual Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7DaysActivity.reversed().forEachIndexed { index, isActive ->
                    val dayLabel = dayLabels.getOrElse(index) { "" }
                    val isToday = (index == 0)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    color = if (isActive) PrimaryGreen.copy(alpha = 0.14f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isToday) 1.5.dp else 0.dp,
                                    color = if (isToday) PrimaryGreen else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isActive) "🌿" else "•",
                                fontSize = if (isActive) 14.sp else 16.sp,
                                color = if (isActive) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
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

package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import androidx.compose.ui.res.stringResource
import com.example.R

/**
 * World-class reusable Reminder Interval Selector & Customizer for Sila.
 * Provides:
 * - One-tap frequency presets (Daily, 3 Days, Weekly, 2 Weeks, Monthly).
 * - Interactive Custom Days Stepper & Slider (1 to 60 days).
 * - Kinship-aware smart hints based on relationship closeness.
 * - Sila Soft UI depth and tactile haptic feedback.
 */
@Composable
fun ReminderIntervalSelector(
    intervalDays: Int,
    onIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    relationshipDegree: String? = null,
    lang: String = "ar"
) {
    val view = LocalView.current
    val standardIntervals = listOf(1, 3, 7, 14, 30)

    val isCustomModeActive = remember(intervalDays) {
        !standardIntervals.contains(intervalDays)
    }
    var showCustomControls by remember(intervalDays) {
        mutableStateOf(!standardIntervals.contains(intervalDays))
    }

    val presets = remember {
        listOf(
            Triple(1, R.string.interval_daily, "⚡"),
            Triple(3, R.string.interval_3_days, "🌿"),
            Triple(7, R.string.interval_weekly, "📅"),
            Triple(14, R.string.interval_2_weeks, "🗓️"),
            Triple(30, R.string.interval_monthly, "🌙")
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.interval_frequency_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Current summary pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Text(
                    text = when (intervalDays) {
                        1 -> stringResource(R.string.interval_daily)
                        3 -> stringResource(R.string.interval_3_days)
                        7 -> stringResource(R.string.interval_weekly)
                        14 -> stringResource(R.string.interval_2_weeks)
                        30 -> stringResource(R.string.interval_monthly)
                        else -> stringResource(R.string.interval_days_formatted, intervalDays)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Preset Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 1.dp)
        ) {
            items(presets) { (days, labelRes, emoji) ->
                val isSelected = intervalDays == days && !showCustomControls
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    label = "chipContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "chipContent"
                )

                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        showCustomControls = false
                        onIntervalChange(days)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = containerColor,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(emoji, fontSize = 11.sp)
                        Text(
                            text = stringResource(labelRes),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }

            // "Custom..." chip
            item {
                val isCustomSelected = showCustomControls || isCustomModeActive
                val customContainer by animateColorAsState(
                    targetValue = if (isCustomSelected) SoftGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    label = "customChipContainer"
                )
                val customContent by animateColorAsState(
                    targetValue = if (isCustomSelected) Color(0xFF946F15) else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "customChipContent"
                )

                Surface(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        showCustomControls = !showCustomControls
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = customContainer,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isCustomSelected) SoftGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = customContent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.interval_custom),
                            fontSize = 11.sp,
                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                            color = customContent
                        )
                    }
                }
            }
        }

        // Custom Days Stepper & Slider Module (Smoothly expands)
        AnimatedVisibility(
            visible = showCustomControls,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.interval_specify_days),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Stepper [-]  [ Days ]  [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    if (intervalDays > 1) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        onIntervalChange(intervalDays - 1)
                                    }
                                },
                                enabled = intervalDays > 1,
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                            }

                            Text(
                                text = stringResource(R.string.interval_days_formatted, intervalDays),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            FilledTonalIconButton(
                                onClick = {
                                    if (intervalDays < 60) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        onIntervalChange(intervalDays + 1)
                                    }
                                },
                                enabled = intervalDays < 60,
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Slider (1 to 60 days)
                    Slider(
                        value = intervalDays.toFloat().coerceIn(1f, 60f),
                        onValueChange = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onIntervalChange(it.toInt().coerceIn(1, 60))
                        },
                        valueRange = 1f..60f,
                        steps = 59,
                        colors = SliderDefaults.colors(
                            thumbColor = SoftGold,
                            activeTrackColor = PrimaryGreen,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    )

                    // Quick Jump chips (5, 10, 20, 45)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.interval_quick_steps),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(5, 10, 20, 45).forEach { targetDay ->
                                Surface(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        onIntervalChange(targetDay)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (intervalDays == targetDay) PrimaryGreen else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = "$targetDay",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (intervalDays == targetDay) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Kinship Advice Tip
        val adviceText = when {
            relationshipDegree in listOf("والدان", "Parents") -> stringResource(R.string.advice_parents)
            relationshipDegree in listOf("أشقاء", "Siblings") -> stringResource(R.string.advice_siblings)
            relationshipDegree in listOf("أعمام/أخوال", "Uncles/Aunts") -> stringResource(R.string.advice_uncles)
            else -> stringResource(R.string.advice_general, intervalDays)
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = PrimaryGreen.copy(alpha = 0.05f),
            border = BorderStroke(0.5.dp, PrimaryGreen.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = SoftGold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = adviceText,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

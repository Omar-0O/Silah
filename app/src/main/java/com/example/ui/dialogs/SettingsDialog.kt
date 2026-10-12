package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel
import com.example.work.ReminderScheduler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import com.example.ui.theme.PrimaryGreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun SettingsDialog(
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit,
    onReplayOnboarding: () -> Unit = {}
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    var showImportConfirm by remember { mutableStateOf(false) }
    val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    val languages = listOf(
        Pair("ar", "🇸🇦 العربية (Arabic)"),
        Pair("en", "🇬🇧 English (الإنجليزية)")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val isTablet = maxWidth > 600.dp
                val dialogWidth = if (isTablet) 540.dp else maxWidth

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.width(dialogWidth).fillMaxWidth()
                ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_dialog_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // ── Language Selector Section ────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_lang_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.settings_lang_desc),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = selectedLanguage == "ar",
                                onClick = { viewModel.selectLanguage("ar") },
                                label = { Text("العربية 🇸🇦", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            FilterChip(
                                selected = selectedLanguage == "en",
                                onClick = { viewModel.selectLanguage("en") },
                                label = { Text("English 🇬🇧", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    HorizontalDivider()

                    // ── Dark Mode Switch ────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.settings_dark_mode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.settings_dark_mode_desc),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }

                    HorizontalDivider()

                    // ── Support Sila Section ────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_support_card_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.settings_support_card_desc),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    onDismiss()
                                    viewModel.openSupportSilaDialog()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.ui.theme.SoftGold,
                                    contentColor = Color(0xFF141816)
                                )
                            ) {
                                Text(
                                    stringResource(R.string.settings_support_btn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // ── Notifications Preferences Section ────────────────────────
                    Text(
                        stringResource(R.string.settings_notif_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val prefDue by viewModel.prefNotifyDueRelatives.collectAsState()
                    val prefEncouragement by viewModel.prefNotifyEncouragement.collectAsState()
                    val prefMonthly by viewModel.prefNotifyMonthly.collectAsState()
                    val reminderHour by viewModel.reminderHour.collectAsState()
                    val reminderMinute by viewModel.reminderMinute.collectAsState()
                    val context = LocalContext.current

                    val formattedReminderTime = remember(reminderHour, reminderMinute, selectedLanguage) {
                        val isAm = reminderHour < 12
                        val displayHour = when {
                            reminderHour == 0 -> 12
                            reminderHour > 12 -> reminderHour - 12
                            else -> reminderHour
                        }
                        val displayMin = String.format("%02d", reminderMinute)
                        if (selectedLanguage == "en") {
                            "$displayHour:$displayMin ${if (isAm) "AM" else "PM"}"
                        } else {
                            "$displayHour:$displayMin ${if (isAm) "ص" else "م"}"
                        }
                    }

                    // Toggle 1: Due Relatives Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_kin_reminders),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.settings_kin_reminders_desc),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefDue,
                            onCheckedChange = { viewModel.toggleNotifyDueRelatives(it) }
                        )
                    }

                    val view = LocalView.current

                    // Daily Reminder Time Picker (shown when Kin Tie Reminders is ON)
                    AnimatedVisibility(
                        visible = prefDue,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                            ),
                            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Header Row with Clock Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                color = PrimaryGreen.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_daily_time),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_daily_time_desc),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Big Digital Time Card (Clickable to pick exact time)
                                Surface(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                viewModel.updateReminderTime(h, m)
                                            },
                                            reminderHour,
                                            reminderMinute,
                                            false
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.35f)),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val isAm = reminderHour < 12
                                            val displayHour = when {
                                                reminderHour == 0 -> 12
                                                reminderHour > 12 -> reminderHour - 12
                                                else -> reminderHour
                                            }
                                            val displayMin = String.format("%02d", reminderMinute)

                                            Text(
                                                text = "$displayHour:$displayMin",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PrimaryGreen,
                                                letterSpacing = 1.sp
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isAm) SoftGold.copy(alpha = 0.2f) else PrimaryGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (isAm) stringResource(R.string.am_marker) else stringResource(R.string.pm_marker),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAm) Color(0xFF946F15) else PrimaryGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.settings_change),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                // Quick Presets Row
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.settings_quick_presets),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val timePresets = listOf(
                                        Pair(13, 0),
                                        Pair(18, 0),
                                        Pair(19, 0),
                                        Pair(20, 30)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        timePresets.forEach { (hour, min) ->
                                            val isPresetActive = reminderHour == hour && reminderMinute == min
                                            Surface(
                                                onClick = {
                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    viewModel.updateReminderTime(hour, min)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isPresetActive) PrimaryGreen else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (isPresetActive) PrimaryGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (selectedLanguage == "en") {
                                                            when (hour) {
                                                                13 -> "1 PM"
                                                                18 -> "6 PM"
                                                                19 -> "7 PM ✨"
                                                                else -> "8:30 PM"
                                                            }
                                                        } else {
                                                            when (hour) {
                                                                13 -> "١ م"
                                                                18 -> "٦ م"
                                                                19 -> "٧ م ✨"
                                                                else -> "٨:٣٠ م"
                                                            }
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isPresetActive) FontWeight.ExtraBold else FontWeight.Medium,
                                                        color = if (isPresetActive) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Golden Hour Guidance Hint
                                    Text(
                                        text = stringResource(R.string.settings_golden_hour),
                                        fontSize = 11.sp,
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                // Note below
                                Text(
                                    text = stringResource(R.string.settings_reminder_note, formattedReminderTime),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    // Toggle 2: Encouragement Messages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_encouragement),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.settings_encouragement_desc),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefEncouragement,
                            onCheckedChange = { viewModel.toggleNotifyEncouragement(it) }
                        )
                    }

                    // Toggle 3: Monthly Reminders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_monthly),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.settings_monthly_desc),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefMonthly,
                            onCheckedChange = { viewModel.toggleNotifyMonthly(it) }
                        )
                    }

                    // Battery Optimization & Background Delivery Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.settings_battery_title),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(R.string.settings_battery_desc),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = {
                                    try {
                                        val intent = ReminderScheduler.createIgnoreBatteryOptimizationIntent(context)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        com.example.core.util.SilaLogger.e("SettingsDialog", "Failed to start battery optimization intent", e)
                                    }
                                }
                            ) {
                                Text(
                                    stringResource(R.string.settings_battery_configure),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // ── Backup & Restore Section ────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.backup_restore),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Info Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = stringResource(R.string.settings_backup_info),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        // Export Button
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                viewModel.triggerExport()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    stringResource(R.string.export_backup),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.settings_export_subtitle, relatives.size),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Import Button
                        OutlinedButton(
                            onClick = { showImportConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, SoftGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB45309))
                        ) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    stringResource(R.string.import_backup),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.settings_import_subtitle),
                                    fontSize = 10.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        // Replay Onboarding Button
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onReplayOnboarding()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                stringResource(R.string.settings_replay_onboarding),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // ── Close Button ────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftGold,
                                contentColor = Color(0xFF141816)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ── Import Confirmation Dialog ──────────────────────────────────────────
    if (showImportConfirm) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            AlertDialog(
                onDismissRequest = { showImportConfirm = false },
                title = {
                    Text(
                        stringResource(R.string.import_backup),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.settings_import_confirm_msg),
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showImportConfirm = false
                            onDismiss()
                            viewModel.triggerImport()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816))
                    ) {
                        Text(
                            stringResource(R.string.settings_import_confirm_btn),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
        }
    }
}

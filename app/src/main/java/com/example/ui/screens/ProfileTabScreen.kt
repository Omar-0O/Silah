package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.ProfilePhotoSheet
import com.example.ui.components.SilaUserAvatar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel
import com.example.work.ReminderScheduler

/**
 * ProfileTabScreen — شاشة الملف الشخصي والإعدادات
 * تظهر عند اختيار تبويب البروفايل من شريط التنقل السفلي.
 * تحتوي في الأعلى على صورة المستخدم واسمه مع إمكانية التعديل،
 * وتتضمن كافة إعدادات التطبيق والتنبيهات والنسخ الاحتياطي في شاشة واحدة متناسقة.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTabScreen(
    viewModel: RelativeViewModel,
    onReplayOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current

    val userName by viewModel.userName.collectAsState()
    val userPhotoPath by viewModel.userPhotoPath.collectAsState()
    val userPhotoTimestamp by viewModel.userPhotoTimestamp.collectAsState()
    val userGender by viewModel.userGender.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val relatives by viewModel.relatives.collectAsState()

    var showAvatarPicker by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    val displayName = if (userName.isNotBlank()) userName
                      else if (selectedLanguage == "en") "User" else "مستخدم صِلَة"

    // Notifications & Reminder States
    val prefDue by viewModel.prefNotifyDueRelatives.collectAsState()
    val prefEncouragement by viewModel.prefNotifyEncouragement.collectAsState()
    val prefMonthly by viewModel.prefNotifyMonthly.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

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

    // Profile Photo Sheet
    if (showAvatarPicker) {
        ProfilePhotoSheet(
            photoPath = userPhotoPath,
            userName = userName,
            timestamp = userPhotoTimestamp,
            lang = selectedLanguage,
            onPhotoSelected = { uri ->
                viewModel.setUserPhoto(uri)
            },
            onRemovePhoto = {
                viewModel.removeUserPhoto()
            },
            onDismiss = { showAvatarPicker = false }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (selectedLanguage == "en") "Edit Your Name" else "تعديل اسمك",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text(if (selectedLanguage == "en") "Your Name" else "اسمك الكريم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditNameDialog = false }) {
                            Text(if (selectedLanguage == "en") "Cancel" else "إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editedName.isNotBlank()) {
                                    viewModel.saveUserProfile(editedName.trim(), userGender)
                                }
                                showEditNameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (selectedLanguage == "en") "Save" else "حفظ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    var showImportConfirm by remember { mutableStateOf(false) }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = {
                Text(
                    text = if (selectedLanguage == "en") "Restore Backup" else "استعادة نسخة احتياطية",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (selectedLanguage == "en")
                        "Choose a backup JSON file. Existing relatives will be kept and only new contacts will be added."
                    else
                        "اختر ملف النسخة الاحتياطية (JSON). سيتم الاحتفاظ بأقاربك الحاليين وإضافة الأقارب الجدد فقط دون مسح أو تكرار."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirm = false
                        viewModel.triggerImport()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816))
                ) {
                    Text(if (selectedLanguage == "en") "Choose File" else "اختيار الملف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) {
                    Text(if (selectedLanguage == "en") "Cancel" else "إلغاء")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
        ) {
            // ── 1. Profile Header Card (Avatar + Name + Edit) ───────────────
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = PrimaryGreen.copy(alpha = 0.08f),
                            spotColor = PrimaryGreen.copy(alpha = 0.04f)
                        ),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.10f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with edit button badge
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier.clickable { showAvatarPicker = true }
                        ) {
                            SilaUserAvatar(
                                photoPath = userPhotoPath,
                                userName = userName,
                                timestamp = userPhotoTimestamp,
                                size = 68.dp,
                                showBorder = true
                            )
                            Surface(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                color = SoftGold,
                                shadowElevation = 3.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Avatar",
                                        tint = Color(0xFF141816),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Name with clickable edit icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    editedName = userName
                                    showEditNameDialog = true
                                }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = displayName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Name",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ── 2. Settings Section Title ────────────────────────────────────
            item {
                Text(
                    text = if (selectedLanguage == "en") "Settings & Preferences ⚙️" else "الإعدادات والتفضيلات ⚙️",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // ── 3. Core Preferences Card (Language & Dark Mode) ─────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Language Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (selectedLanguage == "en") "Language" else "لغة التطبيق",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (selectedLanguage == "en") "Display language" else "اختر لغة عرض الواجهة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = selectedLanguage == "ar",
                                    onClick = { viewModel.selectLanguage("ar") },
                                    label = { Text("العربية 🇸🇦", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = selectedLanguage == "en",
                                    onClick = { viewModel.selectLanguage("en") },
                                    label = { Text("English 🇬🇧", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))

                        // Dark Mode Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (selectedLanguage == "en") "Dark Mode" else "الوضع الداكن",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (selectedLanguage == "en") "Comfortable viewing in low light" else "واجهة مريحة للعين في الإضاءة الخافتة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryGreen
                                )
                            )
                        }
                    }
                }
            }

            // ── 4. Notifications & Reminders Card ────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == "en") "Daily Reminder Time ⏰" else "وقت التذكير اليومي ⏰",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Digital Time Surface Picker
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
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.40f)),
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
                                        color = PrimaryGreen
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isAm) SoftGold.copy(alpha = 0.25f) else PrimaryGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (selectedLanguage == "en") {
                                                if (isAm) "AM" else "PM"
                                            } else {
                                                if (isAm) "صباحاً" else "مساءً"
                                            },
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
                                        .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (selectedLanguage == "en") "Change" else "تعديل",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                        }

                        // Presets Row — Golden Hours (6:00 - 8:00 PM) & Evening Downtime
                        val timePresets = listOf(Pair(13, 0), Pair(18, 0), Pair(19, 0), Pair(20, 30))
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
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (selectedLanguage == "en") {
                                                when (hour) { 13 -> "1 PM"; 18 -> "6 PM"; 19 -> "7 PM ✨"; else -> "8:30 PM" }
                                            } else {
                                                when (hour) { 13 -> "١ م"; 18 -> "٦ م"; 19 -> "٧ م ✨"; else -> "٨:٣٠ م" }
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isPresetActive) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isPresetActive) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Golden Hour Guidance
                        Text(
                            text = if (selectedLanguage == "en")
                                "✨ Golden Hour (6:00 - 8:00 PM): Best for family leisure, avoiding work and sleep."
                            else
                                "✨ التوقيت الذهبي (٦:٠٠ - ٨:٠٠ م): الإرسال في أوقات الفراغ الاجتماعي وتجنب ساعات العمل والنوم.",
                            fontSize = 11.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))

                        // Toggle 1: Due Relatives
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (selectedLanguage == "en") "Due Relatives Alert" else "تنبيه موعد التواصل",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (selectedLanguage == "en") "Notify when relatives are due for kinship" else "تنبيه عند حلول موعد التواصل مع الأقارب",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefDue,
                                onCheckedChange = { viewModel.toggleNotifyDueRelatives(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                            )
                        }

                        // Toggle 2: Encouragement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (selectedLanguage == "en") "Inspirational Quotes" else "رسائل التشجيع والأحاديث",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (selectedLanguage == "en") "Weekly kinship virtues & reminders" else "أحاديث وفضائل صلة الرحم أسبوعياً",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefEncouragement,
                                onCheckedChange = { viewModel.toggleNotifyEncouragement(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                            )
                        }
                    }
                }
            }

            // ── 5. Backup & Restore Card ─────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == "en") "Data & Backup 💾" else "البيانات والنسخ الاحتياطي 💾",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.triggerExport() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedLanguage == "en") "Export JSON" else "نسخ احتياطي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showImportConfirm = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedLanguage == "en") "Import Data" else "استعادة بيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── 6. Support Sila & About ──────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedLanguage == "en") "🤍 Support Sila" else "🤍 ادعم صِلَةِ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (selectedLanguage == "en") "Keep Sila free & ad-free" else "ساعد في بقاء التطبيق مجانياً وخالياً من الإعلانات",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { viewModel.openSupportSilaDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftGold,
                                contentColor = Color(0xFF141816)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (selectedLanguage == "en") "Support" else "ادعم", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // ── 7. Why Maintain Kin Ties Screen ("ليه أصل رحمي؟") ──────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                    ),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.openWhyKinshipScreen()
                        }
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
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(PrimaryGreen.copy(alpha = 0.14f))
                            ) {
                                Text("📖", fontSize = 22.sp)
                            }

                            Column {
                                Text(
                                    text = if (selectedLanguage == "en") "Why Maintain Kin Ties?" else "ليه أصل رحمي؟",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (selectedLanguage == "en") "Answers & guidance from Quran & Sunnah" else "إجابات وبصائر من القرآن الكريم والسنّة",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

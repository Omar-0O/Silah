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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel

@Composable
fun SettingsDialog(
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    var showImportConfirm by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints {
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
                        text = if (selectedLanguage == "en") "Silah Settings ⚙️" else "إعدادات تطبيق صِلَةِ ⚙️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // ── Dark Mode Switch ────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (selectedLanguage == "en") "Dark Mode" else "الوضع الداكن (Dark Mode)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (selectedLanguage == "en") "Eye-friendly interface in low light" else "واجهة مريحة للعين في الإضاءة الخافتة",
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

                    // ── Support Sila Option (Adaptive Card without white background strip) ─────
                    Surface(
                        onClick = {
                            onDismiss()
                            viewModel.showSupportSilaDialog.value = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedLanguage == "en") "Support Sila" else "ادعم صِلَةِ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    // ── Notification Preferences (Clear High-Contrast Toggles) ─────
                    val prefNotifyKinReminders by viewModel.prefNotifyKinReminders.collectAsState()
                    val prefNotifyEncouragement by viewModel.prefNotifyEncouragement.collectAsState()
                    val prefNotifyMonthly by viewModel.prefNotifyMonthly.collectAsState()

                    Text(
                        text = if (selectedLanguage == "en") "Notification Settings:" else "إعدادات الإشعارات والتنبيهات:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedLanguage == "en") "Kin Tie Reminders" else "تذكير صلة الرحم",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = prefNotifyKinReminders,
                                onCheckedChange = { viewModel.toggleKinReminders(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0E7075),
                                    uncheckedTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedLanguage == "en") "Encouragement Messages" else "رسائل التشجيع والإنجازات",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = prefNotifyEncouragement,
                                onCheckedChange = { viewModel.toggleEncouragement(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0E7075),
                                    uncheckedTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedLanguage == "en") "Monthly Reminders" else "التذكيرات الشهرية",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = prefNotifyMonthly,
                                onCheckedChange = { viewModel.toggleMonthly(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF0E7075),
                                    uncheckedTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }

                    // ── Notification Action Buttons ─────────────────────────
                    val prefNotifActionCall by viewModel.prefNotifActionCall.collectAsState()
                    val prefNotifActionWhatsapp by viewModel.prefNotifActionWhatsapp.collectAsState()
                    val prefNotifActionDone by viewModel.prefNotifActionDone.collectAsState()

                    Text(
                        text = if (selectedLanguage == "en") "Notification Actions:" else "أزرار الإشعارات:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (selectedLanguage == "en") "📞 Show Call Button" else "📞 زر الاتصال",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = prefNotifActionCall,
                                    onCheckedChange = { viewModel.toggleNotifActionCall(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = Color(0xFF0E7075)
                                    )
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (selectedLanguage == "en") "💬 Show WhatsApp Button" else "💬 زر واتساب",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = prefNotifActionWhatsapp,
                                    onCheckedChange = { viewModel.toggleNotifActionWhatsapp(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = Color(0xFF25D366)
                                    )
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (selectedLanguage == "en") "✅ Show Done Button" else "✅ زر تم التواصل",
                                    fontSize = 13.sp
                                )
                                Switch(
                                    checked = prefNotifActionDone,
                                    onCheckedChange = { viewModel.toggleNotifActionDone(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = Color(0xFF0E7075)
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // ── Islamic FAQ ────────────────────────────────────────────
                    Surface(
                        onClick = { showFaqDialog = true },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF0E7075).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF0E7075).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (selectedLanguage == "en") "📖 Islamic Q&A" else "📖 أسئلة قرآنية وحديثية",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0E7075)
                                )
                                Text(
                                    text = if (selectedLanguage == "en") "Why maintain family ties?" else "لماذا صلة الرحم؟",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text("←", fontSize = 18.sp, color = Color(0xFF0E7075))
                        }
                    }

                    HorizontalDivider()

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
                            Text(if (selectedLanguage == "en") "Close" else "إغلاق", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showFaqDialog) {
        IslamicFaqDialog(onDismiss = { showFaqDialog = false })
    }

    // ── Import Confirmation Dialog ──────────────────────────────────────────
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = {
                Text(
                    if (selectedLanguage == "en") "Restore from Backup" else "استعادة من نسخة احتياطية",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (selectedLanguage == "en")
                        "New relatives from the backup file will be added. Relatives with the same phone number will not be duplicated.\n\nDo you want to continue?"
                    else
                        "سيتم إضافة الأقارب الجدد من ملف النسخة الاحتياطية. الأقارب الذين لديهم نفس رقم الهاتف لن يتكرروا.\n\nهل تريد المتابعة؟",
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
                        if (selectedLanguage == "en") "Yes, Choose File" else "نعم، اختر الملف",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) {
                    Text(if (selectedLanguage == "en") "Cancel" else "إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

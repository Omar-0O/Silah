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

    val languages = listOf(
        Pair("ar", "🇸🇦 العربية (Arabic)"),
        Pair("en", "🇬🇧 English (الإنجليزية)")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
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

                // ── Language Selection ──────────────────────────────────────
                Text(
                    if (selectedLanguage == "en") "App Language:" else "لغة التطبيق (Language):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    languages.forEach { (langCode, langLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedLanguage == langCode,
                                onClick = { viewModel.selectLanguage(langCode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(langLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                            text = if (selectedLanguage == "en") "Backup & Restore" else "النسخ الاحتياطي والاستعادة",
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
                            text = if (selectedLanguage == "en")
                                "You can export your relatives list as a JSON file and save it to Google Drive or anywhere else, then restore it later even if you change your phone or reinstall the app."
                            else
                                "يمكنك تصدير قائمة أقاربك كملف JSON وحفظه في Google Drive أو أي مكان آخر، ثم استعادتها لاحقاً حتى لو غيّرت هاتفك أو أعدت تثبيت التطبيق.",
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
                                if (selectedLanguage == "en") "Export Backup 📤" else "تصدير نسخة احتياطية 📤",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (selectedLanguage == "en") "${relatives.size} relatives — will be saved as JSON"
                                else "${relatives.size} قريب — سيُحفظ كملف JSON",
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
                                if (selectedLanguage == "en") "Restore from Backup 📥" else "استعادة من نسخة احتياطية 📥",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (selectedLanguage == "en") "Only new entries will be added (no duplicates)"
                                else "ستُضاف الأرقام الجديدة فقط (دون تكرار)",
                                fontSize = 10.sp,
                                color = Color(0xFFB45309)
                            )
                        }
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
                        Text(if (selectedLanguage == "en") "Close" else "إغلاق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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

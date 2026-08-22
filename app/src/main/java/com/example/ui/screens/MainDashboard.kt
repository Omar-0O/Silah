package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Relative
import com.example.ui.components.CallLogBadge
import com.example.ui.dialogs.AddEditRelativeDialog
import com.example.ui.dialogs.ImportContactsDialog
import com.example.ui.dialogs.RecordLogBottomSheet
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeViewModel
import java.text.SimpleDateFormat
import java.util.*

// Helper: pick the right string based on language
fun String.ifEn(lang: String, en: String): String = if (lang == "en") en else this

@Composable
fun AppNavigation(viewModel: RelativeViewModel) {
    val prefs = androidx.compose.ui.platform.LocalContext.current
        .getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
    
    var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }
    var showSplash by remember { mutableStateOf(true) }

    when {
        showSplash -> SplashScreen(onFinished = { showSplash = false })
        showOnboarding -> OnboardingScreen(
            viewModel = viewModel,
            onFinished = {
                prefs.edit().putBoolean("onboarding_done", true).apply()
                showOnboarding = false
            }
        )
        else -> MainDashboardScreen(viewModel = viewModel)
    }
}

@Composable
fun MainDashboardScreen(viewModel: RelativeViewModel) {
    // Dialog state collectors
    val showAddRelativeDialog by viewModel.showAddRelativeDialog.collectAsState()
    val showEditRelativeDialog by viewModel.showEditRelativeDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showImportContactsDialog by viewModel.showImportContactsDialog.collectAsState()
    val showRecordLogDialog by viewModel.showRecordLogDialog.collectAsState()
    val showLogsHistoryDialog by viewModel.showLogsHistoryDialog.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Switch layout direction based on selected language
    val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            RelativesTabScreen(viewModel = viewModel)
        }

        // --- Dialogs & Bottom Sheets ---

        if (showAddRelativeDialog) {
            AddEditRelativeDialog(
                viewModel = viewModel,
                relativeToEdit = null,
                onDismiss = { viewModel.showAddRelativeDialog.value = false }
            )
        }

        if (showEditRelativeDialog != null) {
            AddEditRelativeDialog(
                viewModel = viewModel,
                relativeToEdit = showEditRelativeDialog,
                onDismiss = { viewModel.showEditRelativeDialog.value = null }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showSettingsDialog.value = false }
            )
        }

        if (showRecordLogDialog != null) {
            RecordLogBottomSheet(
                relative = showRecordLogDialog!!,
                viewModel = viewModel,
                onDismiss = { viewModel.showRecordLogDialog.value = null }
            )
        }

        if (showLogsHistoryDialog != null) {
            LogsHistoryDialog(
                relative = showLogsHistoryDialog!!,
                viewModel = viewModel,
                onDismiss = { viewModel.showLogsHistoryDialog.value = null }
            )
        }

        if (showImportContactsDialog) {
            ImportContactsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showImportContactsDialog.value = false }
            )
        }
    }
}

@Composable
fun LogsHistoryDialog(
    relative: Relative,
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val logs by viewModel.logs.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()
    val relativeLogs = logs.filter { it.relativeId == relative.id }.sortedByDescending { it.timestamp }
    val dateLocale = if (lang == "en") Locale.ENGLISH else Locale("ar")
    val dateFormat = remember(lang) { SimpleDateFormat("yyyy/MM/dd - hh:mm a", dateLocale) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (lang == "en") "${relative.name}'s Communication Log 📜"
                           else "سجل تواصل ${relative.name} 📜",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (relativeLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (lang == "en") "No communication recorded yet"
                            else "لم يتم تسجيل تواصل مع هذا القريب بعد",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(relativeLogs) { log ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CallLogBadge(logType = log.type)
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = DateUtils.formatRelativeTimeExact(log.timestamp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = dateFormat.format(Date(log.timestamp)),
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    if (log.notes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = log.notes,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (lang == "en") "Close" else "إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

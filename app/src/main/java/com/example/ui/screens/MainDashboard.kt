package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.dialogs.KinshipCheckInDialog
import com.example.ui.dialogs.MilestoneDialog
import com.example.ui.dialogs.RecordLogBottomSheet
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.SupportSilaDialog
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.ui.components.SilaFloatingNavigationBar
import com.example.ui.components.SilaNavBarScrollState
import com.example.ui.components.rememberSilaNavBarScrollState
import com.example.ui.components.SilaTab
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.hazeSource

// Helper: pick the right string based on language
fun String.ifEn(lang: String, en: String): String = if (lang == "en") en else this

// ── App Navigation (Splash → Onboarding → Main) ───────────────────────────────
@Composable
fun AppNavigation(viewModel: RelativeViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    
    var onboardingDone by remember {
        mutableStateOf(
            try { prefs.getBoolean("onboarding_done", false) } catch (e: Exception) { false }
        )
    }

    var forceShowOnboarding by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    // Show onboarding if not completed yet, OR if manually requested
    val showOnboarding = forceShowOnboarding || !onboardingDone

    when {
        showSplash -> SplashScreen(onFinished = { showSplash = false })
        showOnboarding -> OnboardingScreen(
            viewModel = viewModel,
            onFinished = {
                prefs.edit().putBoolean("onboarding_done", true).commit()
                onboardingDone = true
                forceShowOnboarding = false
            }
        )
        else -> MainDashboardScreen(
            viewModel = viewModel,
            onReplayOnboarding = { forceShowOnboarding = true }
        )
    }
}

// ── Main Dashboard Screen with Bottom Nav ────────────────────────────────────
@Composable
fun MainDashboardScreen(
    viewModel: RelativeViewModel,
    onReplayOnboarding: () -> Unit = {}
) {
    val showAddRelativeDialog by viewModel.showAddRelativeDialog.collectAsState()
    val showEditRelativeDialog by viewModel.showEditRelativeDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showImportContactsDialog by viewModel.showImportContactsDialog.collectAsState()
    val showRecordLogDialog by viewModel.showRecordLogDialog.collectAsState()
    val showLogsHistoryDialog by viewModel.showLogsHistoryDialog.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    val showKinshipCheckInDialog by viewModel.showKinshipCheckInDialog.collectAsState()
    val pendingNotifiedRelatives by viewModel.pendingNotifiedRelatives.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPendingNotifiedRelatives()
    }

    val userName by viewModel.userName.collectAsState()
    val userPhotoPath by viewModel.userPhotoPath.collectAsState()
    val userPhotoTimestamp by viewModel.userPhotoTimestamp.collectAsState()
    val userAvatarId by viewModel.userAvatarId.collectAsState()

    val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    // Current selected tab
    var selectedTab by remember { mutableStateOf(SilaTab.DASHBOARD) }

    // Due relatives count for badge
    val dueCount = remember(relatives) {
        relatives.count { r ->
            val diffDays = if (r.lastContactDate == 0L) Int.MAX_VALUE
                           else ((System.currentTimeMillis() - r.lastContactDate) / 86400000).toInt()
            diffDays >= r.contactIntervalDays
        }
    }

    val selectedRelativeForDetail by viewModel.selectedRelativeForDetail.collectAsState()
    val showWhyKinshipScreen by viewModel.showWhyKinshipScreen.collectAsState()

    if (showWhyKinshipScreen) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            WhyKinshipScreen(
                lang = selectedLanguage,
                onBack = { viewModel.closeWhyKinshipScreen() }
            )
        }
        return
    }

    if (selectedRelativeForDetail != null) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            RelativeDetailScreen(
                relative = selectedRelativeForDetail!!,
                viewModel = viewModel,
                onBack = { viewModel.clearSelectedRelative() }
            )
        }
        return
    }

    val navBarScrollState = rememberSilaNavBarScrollState()
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Content with nested scroll connection for responsive navbar + hazeSource for background blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(navBarScrollState.nestedScrollConnection)
                    .hazeSource(state = hazeState)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInHorizontally { w ->
                            if (targetState.ordinal > initialState.ordinal) w / 6 else -w / 6
                        }).togetherWith(
                            fadeOut(tween(180)) + slideOutHorizontally { w ->
                                if (targetState.ordinal > initialState.ordinal) -w / 6 else w / 6
                            }
                        )
                    },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        SilaTab.DASHBOARD -> HomeTabScreen(viewModel = viewModel)
                        SilaTab.RELATIVES -> RelativesTabScreen(viewModel = viewModel)
                        SilaTab.PROFILE   -> ProfileTabScreen(
                            viewModel = viewModel,
                            onReplayOnboarding = onReplayOnboarding
                        )
                    }
                }
            }

            // Floating Navigation Bar overlaid at bottom center with blur effect
            SilaFloatingNavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                },
                lang = selectedLanguage,
                dueCount = dueCount,
                userPhotoPath = userPhotoPath,
                userName = userName,
                userPhotoTimestamp = userPhotoTimestamp,
                userAvatarId = userAvatarId,
                scrollState = navBarScrollState,
                hazeState = hazeState
            )

            // ── Global Dialogs & Bottom Sheets ────────────────────────────────
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
            val currentRecordLog = showRecordLogDialog
            if (currentRecordLog != null) {
                RecordLogBottomSheet(
                    relative = currentRecordLog,
                    viewModel = viewModel,
                    onDismiss = { viewModel.showRecordLogDialog.value = null }
                )
            }
            val currentLogsHistory = showLogsHistoryDialog
            if (currentLogsHistory != null) {
                LogsHistoryDialog(
                    relative = currentLogsHistory,
                    viewModel = viewModel,
                    onDismiss = { viewModel.showLogsHistoryDialog.value = null }
                )
            }
            val showSupportSilaDialog by viewModel.showSupportSilaDialog.collectAsState()
            val activeMilestoneDialog by viewModel.activeMilestoneDialog.collectAsState()
            val logs by viewModel.logs.collectAsState()

            if (showSupportSilaDialog) {
                val contactedCount = relatives.count { it.lastContactDate > 0 }
                val prefs = androidx.compose.ui.platform.LocalContext.current
                    .getSharedPreferences("silah_prefs", android.content.Context.MODE_PRIVATE)
                val firstLaunch = prefs.getLong("app_first_launch_time", System.currentTimeMillis())
                val daysUsingApp = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - firstLaunch
                )

                com.example.ui.dialogs.SupportSilaDialog(
                    contactedCount = contactedCount,
                    interactionCount = logs.size,
                    daysUsingApp = daysUsingApp,
                    lang = selectedLanguage,
                    onDismiss = { viewModel.showSupportSilaDialog.value = false }
                )
            }

            val currentMilestone = activeMilestoneDialog
            if (currentMilestone != null) {
                MilestoneDialog(
                    milestoneCount = currentMilestone,
                    onSupportClick = {
                        viewModel.activeMilestoneDialog.value = null
                        viewModel.showSupportSilaDialog.value = true
                    },
                    onDismiss = { viewModel.activeMilestoneDialog.value = null }
                )
            }

            if (showKinshipCheckInDialog && pendingNotifiedRelatives.isNotEmpty()) {
                KinshipCheckInDialog(
                    relatives = pendingNotifiedRelatives,
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissKinshipCheckInDialog() }
                )
            }
        }
    }
}


// ── Logs History Dialog ───────────────────────────────────────────────────────
@Composable
fun LogsHistoryDialog(
    relative: Relative,
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val logs by viewModel.logs.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()
    val layoutDirection = if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
    // BUG-10 Fix: collect only this relative's logs directly — avoids loading all logs into memory
    val relativeLogs by remember(relative.id) {
        viewModel.getLogsForRelative(relative.id)
    }.collectAsState(initial = emptyList())
    val dateLocale = if (lang == "en") Locale.ENGLISH else Locale.forLanguageTag("ar")
    val dateFormat = remember(lang) { SimpleDateFormat("yyyy/MM/dd - hh:mm a", dateLocale) }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
            Column(modifier = Modifier.padding(24.dp)) {
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
                        modifier = Modifier.weight(1f).fillMaxWidth(),
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
                        items(relativeLogs, key = { log -> "${log.id}_${log.timestamp}" }) { log ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftGold,
                        contentColor = Color(0xFF141816)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (lang == "en") "Close" else "إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
}

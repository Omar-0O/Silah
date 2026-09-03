package com.example.ui.screens

import android.annotation.SuppressLint
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

// Helper: pick the right string based on language
fun String.ifEn(lang: String, en: String): String = if (lang == "en") en else this

// ── Bottom Nav Tab Enum ───────────────────────────────────────────────────────
enum class SilaTab(
    val labelAr: String,
    val labelEn: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
) {
    DASHBOARD(
        labelAr = "الرئيسية",
        labelEn = "Dashboard",
        iconOutlined = Icons.Outlined.Home,
        iconFilled = Icons.Filled.Home
    ),
    RELATIVES(
        labelAr = "الأرحام",
        labelEn = "Relatives",
        iconOutlined = Icons.Outlined.People,
        iconFilled = Icons.Filled.People
    ),
    SETTINGS(
        labelAr = "الإعدادات",
        labelEn = "Settings",
        iconOutlined = Icons.Outlined.Settings,
        iconFilled = Icons.Filled.Settings
    );

    fun label(lang: String) = if (lang == "en") labelEn else labelAr
}

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

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            bottomBar = {
                SilaBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        // Settings tab opens dialog instead of navigating
                        if (tab == SilaTab.SETTINGS) {
                            viewModel.showSettingsDialog.value = true
                        } else {
                            selectedTab = tab
                        }
                    },
                    lang = selectedLanguage,
                    dueCount = dueCount
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                // Tab Content
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
                        SilaTab.SETTINGS  -> HomeTabScreen(viewModel = viewModel) // BUG-04 Fix: SETTINGS opens dialog, fallback to Dashboard
                    }
                }
            }

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
        }
    }
}

// ── Sila Bottom Navigation Bar ────────────────────────────────────────────────
@Composable
fun SilaBottomNavigationBar(
    selectedTab: SilaTab,
    onTabSelected: (SilaTab) -> Unit,
    lang: String,
    dueCount: Int = 0
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = PrimaryGreen.copy(alpha = 0.12f),
                spotColor = PrimaryGreen.copy(alpha = 0.08f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SilaTab.entries.forEach { tab ->
                if (tab == SilaTab.SETTINGS) {
                    // Settings: always outlined, opens dialog
                    NavItem(
                        tab = tab,
                        isSelected = false,
                        lang = lang,
                        badge = null,
                        onClick = { onTabSelected(tab) }
                    )
                } else {
                    NavItem(
                        tab = tab,
                        isSelected = selectedTab == tab,
                        lang = lang,
                        badge = if (tab == SilaTab.RELATIVES && dueCount > 0) dueCount else null,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    tab: SilaTab,
    isSelected: Boolean,
    lang: String,
    badge: Int?,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
        animationSpec = tween(250),
        label = "nav_color"
    )
    val animatedBgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(250),
        label = "nav_bg"
    )

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onClick() }
    ) {
        // Top indicator line when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(20.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                    .background(PrimaryGreen)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Badge + Icon
            BadgedBox(
                badge = {
                    if (badge != null && badge > 0) {
                        Badge(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White,
                            modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                        ) {
                            Text(
                                text = if (badge > 99) "99+" else badge.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSelected) tab.iconFilled else tab.iconOutlined,
                    contentDescription = tab.label(lang),
                    tint = animatedColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Label
            Text(
                text = tab.label(lang),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = animatedColor
            )
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

package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.components.CommitmentHeaderCard
import com.example.ui.components.KinshipKnotIcon
import com.example.ui.components.RelativeCard
import com.example.ui.components.SilaEmptyStateView
import androidx.compose.foundation.border
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelativesTabScreen(
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allRelatives by viewModel.relatives.collectAsState()
    val filteredRelatives by viewModel.filteredRelatives.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isSyncingCallLogs by viewModel.isSyncingCallLogs.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()

    val allLogs by viewModel.logs.collectAsState()

    // Kin-tie stats derived from communication logs
    val totalLogsCount = allLogs.size
    val uniqueDaysCount = remember(allLogs) {
        allLogs
            .map { log ->
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = log.timestamp
                // Key = year + dayOfYear, to get unique calendar days
                "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
            }
            .toSet()
            .size
    }
    val uniqueRelativesContacted = remember(allLogs) {
        allLogs.map { it.relativeId }.toSet().size
    }

    // Relatives due for contact (still used for status logic)
    val dueRelatives = allRelatives.filter { relative ->
        viewModel.getRelativeStatus(relative) != RelativeStatus.CONNECTED
    }

    val userName by viewModel.userName.collectAsState()

    // Permission launcher for Contacts
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchDeviceContacts(context)
            viewModel.showImportContactsDialog.value = true
        } else {
            Toast.makeText(
                context,
                if (lang == "en") "Contacts permission is required to import relatives"
                else "صلاحية قراءة جهات الاتصال مطلوبة لاستيراد الأقارب",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val launchImportContacts: () -> Unit = {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.fetchDeviceContacts(context)
            viewModel.showImportContactsDialog.value = true
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Permission launcher for Call Logs
    val callLogPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncCallLogsWithRelatives(context) { synced ->
                Toast.makeText(
                    context,
                    if (lang == "en") "Synced $synced new calls automatically! 📞"
                    else "تمت مزامنة $synced اتصالات جديدة تلقائياً! 📞",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                if (lang == "en") "Call log permission is required to track incoming and outgoing calls automatically"
                else "صلاحية سجل المكالمات مطلوبة للتعرف التلقائي على الاتصالات الواردة والصادرة",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Permission launcher for Notification (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* نتيجة طلب الصلاحية — لا نحتاج action خاص */ }

    // Safely sync call logs if permission is granted, avoiding concurrent permission launcher calls at startup
    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.syncCallLogsWithRelatives(context)
        }
    }

    val userGender by viewModel.userGender.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedRelativeForDetail by remember { mutableStateOf<com.example.data.Relative?>(null) }

    // Internal values stay Arabic (used for filtering stored data)
    val categories = listOf("الكل", "والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val categoryLabels = if (lang == "en")
        listOf("All", "Parents", "Siblings", "Uncles/Aunts", "Other Relatives")
    else
        categories

    if (showProfileDialog) {
        com.example.ui.dialogs.UserProfileDialog(
            viewModel = viewModel,
            onDismiss = { showProfileDialog = false }
        )
    }

    // Navigate to detail screen if a relative is selected
    val currentDetailRelative = selectedRelativeForDetail
    if (currentDetailRelative != null) {
        RelativeDetailScreen(
            relative = currentDetailRelative,
            viewModel = viewModel,
            onBack = { selectedRelativeForDetail = null }
        )
        return
    }


    val userAvatarId by viewModel.userAvatarId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Top-Right Header User Profile & Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showProfileDialog = true }
                            .padding(4.dp)
                    ) {
                        com.example.ui.components.SilaUserAvatar(
                            avatarId = userAvatarId,
                            size = 42.dp,
                            showBorder = true
                        )

                        Text(
                            text = if (userName.isNotBlank()) userName else if (lang == "en") "Family Keeper" else "حافظ الأرحام", // BUG-07 Fix: removed hardcoded "عمر"
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = launchImportContacts,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("import_contacts_button"),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = if (lang == "en") "Import" else "استيراد", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (lang == "en") "Import" else "استيراد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddRelativeDialog.value = true },
                containerColor = SoftGold,
                contentColor = Color(0xFF1C221E),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = SoftGold.copy(alpha = 0.4f))
                    .testTag("add_relative_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة قريب جديد", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Commitment Header Card
            item {
                CommitmentHeaderCard(
                    totalLogsCount = totalLogsCount,
                    uniqueDaysCount = uniqueDaysCount,
                    uniqueRelativesContacted = uniqueRelativesContacted,
                    lang = lang,
                    userName = userName,
                    totalRelativesCount = allRelatives.size
                )
            }

            // 3.5 Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories.indices.toList()) { i ->
                        val isSelected = selectedCategory == categories[i]
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surface,
                            animationSpec = tween(200),
                            label = "chip_bg_$i"
                        )
                        val chipText by animateColorAsState(
                            targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            animationSpec = tween(200),
                            label = "chip_text_$i"
                        )
                        Surface(
                            onClick = { viewModel.selectedCategory.value = categories[i] },
                            shape = RoundedCornerShape(50.dp),
                            color = chipBg,
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                            shadowElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Text(
                                text = categoryLabels[i],
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 4. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text(if (lang == "en") "Search by name or phone number..." else "ابحث عن قريب بالاسم أو رقم الهاتف...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = if (lang == "en") "Search" else "بحث") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = SoftGold.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
            }

            // 5. Relatives List
            if (filteredRelatives.isEmpty()) {
                item {
                    if (searchQuery.isNotEmpty()) {
                        SilaEmptyStateView(
                            title = if (lang == "en") "No results matching \"$searchQuery\""
                                    else "لا توجد نتائج مطابقة لـ \"$searchQuery\"",
                            subtitle = if (lang == "en") "Try searching with a different name or phone number"
                                       else "جرّب البحث باسم مختلف أو رقم الهاتف"
                        )
                    } else {
                        SilaEmptyStateView(
                            lang = lang,
                            onImportContactsClick = launchImportContacts,
                            onAddRelativeClick = {
                                viewModel.showAddRelativeDialog.value = true
                            }
                        )
                    }
                }
            } else {
                items(filteredRelatives, key = { relative -> "${relative.id}_${relative.name}_${relative.phone}" }) { relative ->
                    RelativeCard(
                        relative = relative,
                        viewModel = viewModel,
                        onCardClick = { selectedRelativeForDetail = relative }
                    )
                }
            }
        }
    }
}

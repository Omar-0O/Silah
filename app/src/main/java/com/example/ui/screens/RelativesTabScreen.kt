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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.components.CommitmentHeaderCard
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
    val dueRelatives by remember(viewModel) {
        derivedStateOf {
            allRelatives.filter { relative ->
                viewModel.getRelativeStatus(relative) != RelativeStatus.CONNECTED
            }
        }
    }

    val userName by viewModel.userName.collectAsState()

    val launchImportContacts: () -> Unit = {
        viewModel.launchContactPicker()
    }

    // Permission launcher for Call Logs
    val callLogPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncCallLogsWithRelatives(context) { synced ->
                Toast.makeText(
                    context,
                    context.resources.getQuantityString(com.example.R.plurals.synced_calls_count, synced, synced),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                context.getString(com.example.R.string.call_log_perm_required),
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
    val selectedRelativeForDetail by viewModel.selectedRelativeForDetail.collectAsState()

    // Internal values stay Arabic (used for filtering stored data)
    val categories = listOf("الكل", "والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val categoryLabels = listOf(
        stringResource(com.example.R.string.category_all),
        stringResource(com.example.R.string.category_parents),
        stringResource(com.example.R.string.category_siblings),
        stringResource(com.example.R.string.category_uncles_aunts),
        stringResource(com.example.R.string.category_other)
    )

    // Navigate to detail screen if a relative is selected
    val currentDetailRelative = selectedRelativeForDetail
    if (currentDetailRelative != null) {
        RelativeDetailScreen(
            relative = currentDetailRelative,
            viewModel = viewModel,
            onBack = { viewModel.clearSelectedRelative() }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddRelativeDialog.value = true },
                containerColor = SoftGold,
                contentColor = Color(0xFF1C221E),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 76.dp, end = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = SoftGold.copy(alpha = 0.4f))
                    .testTag("add_relative_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(com.example.R.string.add_relative_fab_desc), modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
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

            // 4. Search Bar & Import Contacts Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text(stringResource(com.example.R.string.search_hint), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(com.example.R.string.search)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(com.example.R.string.clear))
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .shadow(3.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = SoftGold.copy(alpha = 0.35f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )

                    Surface(
                        onClick = launchImportContacts,
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("import_contacts_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = stringResource(com.example.R.string.import_text),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(com.example.R.string.import_text),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // 5. Relatives List
            if (filteredRelatives.isEmpty()) {
                item {
                    if (searchQuery.isNotEmpty()) {
                        SilaEmptyStateView(
                            title = stringResource(com.example.R.string.no_search_results_title, searchQuery),
                            subtitle = stringResource(com.example.R.string.no_search_results_subtitle)
                        )
                    } else if (selectedCategory != "الكل" && allRelatives.isNotEmpty()) {
                        SilaEmptyStateView(
                            title = stringResource(com.example.R.string.no_category_results_title),
                            subtitle = stringResource(com.example.R.string.no_category_results_subtitle)
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
                items(filteredRelatives, key = { it.id }) { relative ->
                    RelativeCard(
                        relative = relative,
                        viewModel = viewModel,
                        onCardClick = { viewModel.selectRelativeForDetail(relative) }
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.components.CommitmentHeaderCard
import com.example.ui.components.DueRelativesCarousel
import com.example.ui.components.KinshipKnotIcon
import com.example.ui.components.RelativeCard
import com.example.ui.components.SilaEmptyStateView
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

    val memoriesList by viewModel.memories.collectAsState()
    val surpriseMemory = remember(memoriesList) { memoriesList.randomOrNull() }

    // Dynamic commitment statistics
    val totalRelativesCount = allRelatives.size
    val contactedThisMonthCount = allRelatives.count { relative ->
        relative.lastContactDate > 0L && (System.currentTimeMillis() - relative.lastContactDate) <= (1000L * 60 * 60 * 24 * 30)
    }
    val commitmentPercentage = if (totalRelativesCount > 0) {
        ((contactedThisMonthCount.toFloat() / totalRelativesCount.toFloat()) * 100).toInt()
    } else 0

    // Relatives due for contact
    val dueRelatives = allRelatives.filter { relative ->
        viewModel.getRelativeStatus(relative) != RelativeStatus.CONNECTED
    }

    // Permission launcher for Contacts
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchDeviceContacts(context)
            viewModel.showImportContactsDialog.value = true
        } else {
            Toast.makeText(context, "صلاحية قراءة جهات الاتصال مطلوبة لاستيراد الأقارب", Toast.LENGTH_LONG).show()
        }
    }

    // Permission launcher for Call Logs
    val callLogPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncCallLogsWithRelatives(context) { synced ->
                Toast.makeText(context, "تمت مزامنة $synced اتصالات جديدة تلقائياً! 📞", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "صلاحية سجل المكالمات مطلوبة للتعرف التلقائي على الاتصالات الواردة والصادرة", Toast.LENGTH_LONG).show()
        }
    }

    // Permission launcher for Notification (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* نتيجة طلب الصلاحية — لا نحتاج action خاص */ }

    // اطلب صلاحية الإشعارات عند أول تشغيل (Android 13+ فقط)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // إذا كانت قائمة الأقارب فارغة لأول مرة، افتح واجهة استيراد جهات الاتصال مباشرة
        if (allRelatives.isEmpty()) {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val categories = listOf("الكل", "والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KinshipKnotIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "صِلَةِ",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showSettingsDialog.value = true },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        },
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
                        Icon(Icons.Default.ContactPhone, contentDescription = "استيراد", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استيراد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // 1. Commitment Header Card with Call Log Auto Sync
            item {
                CommitmentHeaderCard(
                    commitmentPercentage = commitmentPercentage,
                    isSyncingCallLogs = isSyncingCallLogs,
                    onSyncCallLogsClick = {
                        callLogPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
                    }
                )
            }

            // 2. Carousel: Relatives due for contact
            item {
                DueRelativesCarousel(
                    dueRelatives = dueRelatives,
                    viewModel = viewModel
                )
            }

            // 3. Family Time Capsule Memory Card
            if (surpriseMemory != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = SoftGold.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                        border = BorderStroke(1.5.dp, SoftGold.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
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
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "مِنْ كَبْسولَةِ الزَّمَنِ العَائِلِيَّةِ ⏳",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFEF3C7))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "ذكرى طيّبة 🤍",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = surpriseMemory.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (surpriseMemory.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = surpriseMemory.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("ابحث عن قريب بالاسم أو رقم الهاتف...") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "بحث") },
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectedCategory.value = category },
                                label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
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
                            title = "لا توجد نتائج مطابقة لـ \"$searchQuery\"",
                            subtitle = "جرّب البحث باسم مختلف أو رقم الهاتف"
                        )
                    } else {
                        SilaEmptyStateView(
                            onImportContactsClick = {
                                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            },
                            onAddRelativeClick = {
                                viewModel.showAddRelativeDialog.value = true
                            }
                        )
                    }
                }
            } else {
                items(filteredRelatives) { relative ->
                    RelativeCard(
                        relative = relative,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

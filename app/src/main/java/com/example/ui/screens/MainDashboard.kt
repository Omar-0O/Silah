package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CommunicationLog
import com.example.data.QuickTemplate
import com.example.data.Relative
import com.example.viewmodel.RelativeStatus
import com.example.viewmodel.RelativeViewModel
import com.example.ui.theme.getFontFamily
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Helper function to format date
fun formatRelativeDate(timestamp: Long): String {
    if (timestamp == 0L) return "لم يتم التواصل معه قط"
    val diffMs = System.currentTimeMillis() - timestamp
    val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

    return when {
        diffDays == 0 -> "اليوم"
        diffDays == 1 -> "أمس"
        diffDays == 2 -> "منذ يومين"
        diffDays in 3..10 -> "منذ $diffDays أيام"
        else -> "منذ ${diffDays / 7} أسبوع"
    }
}

@Composable
fun AppNavigation(viewModel: RelativeViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
    } else {
        MainDashboardScreen(viewModel = viewModel)
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val verses = listOf(
        "«وَاتَّقُوا اللَّهَ الَّذِي تَسَاءَلُونَ بِهِ وَالْأَرْحَامَ ۚ إِنَّ اللَّهَ كَانَ عَلَيْكُمْ رَقِيبًا»\n[النساء: 1]",
        "«وَالَّذِينَ يَصِلُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيَخْشَوْنَ رَبَّهُمْ»\n[الرعد: 21]",
        "«مَنْ سَرَّهُ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ، وَأَنْ يُنْسَأَ لَهُ فِي أَثَرِهِ، فَلْيَصِلْ رَحِمَهُ»\n[حديث شريف]"
    )
    var currentVerseIndex by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(true) }

    // Pulsing animation for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        // Cycle verses
        delay(1400)
        visible = false
        delay(300)
        currentVerseIndex = 1
        visible = true
        delay(1400)
        visible = false
        delay(300)
        currentVerseIndex = 2
        visible = true
        delay(1700)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F3620),
                        Color(0xFF1E5A35),
                        Color(0xFF143B23)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth()
        ) {
            // Gold floral pattern inspired emblem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0x15E9CE79))
                    .border(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "صلة",
                        tint = Color(0xFFE9CE79),
                        modifier = Modifier
                            .size(54.dp)
                            .animateContentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "صِلَةِ الرَّحِم",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Text(
                text = "وَأُولُو الْأَرْحَامِ بَعْضُهُمْ أَوْلَىٰ بِبَعْضٍ فِي كِتَابِ اللَّهِ",
                fontSize = 13.sp,
                color = Color(0xFFBCCEC3),
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(56.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.95f),
                exit = fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 0.95f)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x1FFFFFFF)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.3f)),
                    border = CardDefaults.outlinedCardBorder(enabled = true).copy(
                        brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.03f)))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = verses[currentVerseIndex],
                            fontSize = 16.sp,
                            color = Color(0xFFE9CE79),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KinshipKnotIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.5f)
            cubicTo(w * 0.25f, h * 0.2f, w * 0.45f, h * 0.2f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.55f, h * 0.8f, w * 0.75f, h * 0.8f, w * 0.75f, h * 0.5f)
            cubicTo(w * 0.75f, h * 0.2f, w * 0.55f, h * 0.2f, w * 0.5f, h * 0.5f)
            cubicTo(w * 0.45f, h * 0.8f, w * 0.25f, h * 0.8f, w * 0.25f, h * 0.5f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        drawCircle(color = color, radius = 2.5f.dp.toPx(), center = Offset(w * 0.25f, h * 0.5f))
        drawCircle(color = color, radius = 2.5f.dp.toPx(), center = Offset(w * 0.75f, h * 0.5f))
        drawCircle(color = Color(0xFFE9CE79), radius = 2f.dp.toPx(), center = Offset(w * 0.5f, h * 0.5f))
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainDashboardScreen(viewModel: RelativeViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Enforce Arabic RTL layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Outlined.People, contentDescription = "الأقارب") },
                        label = { Text("الأقارب") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Outlined.StarOutline, contentDescription = "اللوحة والتحديات") },
                        label = { Text("التحديات") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Outlined.Mail, contentDescription = "رسائل جاهزة") },
                        label = { Text("قوالب رسائل") }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> RelativesTab(viewModel = viewModel)
                    1 -> ChallengesTab(viewModel = viewModel)
                    2 -> TemplatesTab(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelativesTab(viewModel: RelativeViewModel) {
    val context = LocalContext.current
    val allRelatives by viewModel.relatives.collectAsState()
    val relatives by viewModel.filteredRelatives.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    val memoriesList by viewModel.memories.collectAsState()
    val surpriseMemory = remember(memoriesList) { memoriesList.randomOrNull() }

    // Calculate dynamic commitment statistics
    val totalRelativesCount = allRelatives.size
    val contactedThisMonthCount = allRelatives.count { relative ->
        relative.lastContactDate > 0L && (System.currentTimeMillis() - relative.lastContactDate) <= (1000L * 60 * 60 * 24 * 30)
    }
    val commitmentPercentage = if (totalRelativesCount > 0) {
        ((contactedThisMonthCount.toFloat() / totalRelativesCount.toFloat()) * 100).toInt()
    } else {
        0
    }

    // Filter relatives who are due for contact (Overdue or Needs Contact)
    val dueRelatives = allRelatives.filter { relative ->
        val status = viewModel.getRelativeStatus(relative)
        status != RelativeStatus.CONNECTED
    }

    // Permission launcher for contacts
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
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            "صِلَةِ الرَّحِم",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp
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
                        Icon(Icons.Default.ContactPhone, contentDescription = "استيراد جهات الاتصال", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استيراد الأقارب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddRelativeDialog.value = true },
                containerColor = Color(0xFFD5BE72),
                contentColor = Color(0xFF1C221E),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFD5BE72).copy(alpha = 0.4f))
                    .testTag("add_relative_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة قريب جديد", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Adaptable Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Dynamic Commitment Header widget
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF436F51),
                                    Color(0xFF2E513A)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                "مَسيرَةُ صِلَتِكِ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE9CE79),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "وَصِلْ مَنْ قَطَعَكَ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (commitmentPercentage >= 70) {
                                    "طوبى لكِ! صلة أرحامك تزيد في عمرك وتبارك رزقكِ. استمري في هذا العطاء الجميل 🌸"
                                } else if (commitmentPercentage >= 40) {
                                    "بداية طيبة ومباركة! بقي القليل من الأقارب بانتظار تواصلك معهم وبث الود في قلوبهم ✨"
                                } else {
                                    "إن صلة الرحم معلقة بالعرش تقول: من وصلني وصله الله. ابدئي اليوم بخطوات بسيطة دافئة 💚"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFFBCCEC3),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(88.dp)
                                .weight(0.7f)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0x22FFFFFF),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    color = Color(0xFFE9CE79),
                                    startAngle = -90f,
                                    sweepAngle = (commitmentPercentage * 3.6).toFloat(),
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$commitmentPercentage%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "الالتزام",
                                    fontSize = 10.sp,
                                    color = Color(0xFFBCCEC3)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Horizontal "Due for Contact" carousel
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (dueRelatives.isNotEmpty()) Color(0xFFE57373) else Color(0xFF81C784))
                        )
                        Text(
                            text = "حان وقتُ وَصْلِهِم",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (dueRelatives.isNotEmpty()) {
                            Badge(
                                containerColor = Color(0xFFE57373).copy(alpha = 0.2f),
                                contentColor = Color(0xFFD32F2F),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(" ${dueRelatives.size} ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (dueRelatives.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECF5F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF94DAB2).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF1E5A35),
                                    modifier = Modifier.size(36.dp)
                                )
                                Column {
                                    Text(
                                        "جميع أرحامكِ موصولون بالكامل! 🎉",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E5A35)
                                    )
                                    Text(
                                        "ما شاء الله، التزامكِ رائع ويقرب المسافات. طابت أيامكِ ببركة الود والرحمة.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4C6B56)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            items(dueRelatives) { relative ->
                                val status = viewModel.getRelativeStatus(relative)
                                val statusBgColor = Color(android.graphics.Color.parseColor("#15" + status.colorHex))
                                val statusTextColor = Color(android.graphics.Color.parseColor("#FF" + status.colorHex))

                                Card(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                                        .clickable { viewModel.showLogsHistoryDialog.value = relative },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color(0xFFE9CE79).copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(statusBgColor)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = status.label,
                                                    color = statusTextColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = relative.relationshipDegree,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = relative.name,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Quick Action: WhatsApp
                                                IconButton(
                                                    onClick = {
                                                        var cleanPhone = relative.phone.replace("\\s|-|\\(|\\)".toRegex(), "")
                                                        if (!cleanPhone.startsWith("+") && !cleanPhone.startsWith("00")) {
                                                            if (cleanPhone.startsWith("0")) {
                                                                cleanPhone = "966" + cleanPhone.substring(1)
                                                            }
                                                        }
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                                                        }
                                                        context.startActivity(intent)
                                                        viewModel.recordCommunication(relative.id, "رسالة", "تواصل سريع ومباشر عبر الواتساب")
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color(0xFFE8F5E9), CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Chat,
                                                        contentDescription = "واتساب",
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                // Quick Action: Record Log
                                                IconButton(
                                                    onClick = { viewModel.showRecordLogDialog.value = relative },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.CheckCircle,
                                                        contentDescription = "سجل",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "آخر تواصل: ${formatRelativeDate(relative.lastContactDate)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SMART FEATURE 3: Surprise Memory Card (ذكريات طيبة مفاجئة)
            if (surpriseMemory != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFFE9CE79).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                        border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.4f))
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
                                        tint = Color(0xFFD5BE72),
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Text(
                                        text = surpriseMemory.relativeName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "بادر بوصله وتجديد العهد ✨",
                                    fontSize = 11.sp,
                                    color = Color(0xFF436F51),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search and Full List Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "قائِمتُكِ الكامِلَة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Search Input (Styled Beautifully without harsh borders)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_relative_input"),
                        placeholder = { Text("ابحث باسم القريب أو رقم هاتفه...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color(0xFFEEEDE7),
                            disabledBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Categories Filter Tabs
                    val categories = listOf("الكل", "والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory),
                        edgePadding = 0.dp,
                        divider = {},
                        indicator = {},
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Card(
                                modifier = Modifier
                                    .padding(end = 8.dp, bottom = 4.dp)
                                    .clickable { viewModel.selectedCategory.value = cat },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFEEEDE7))
                            ) {
                                Text(
                                    text = cat,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Full List content (Nested inside LazyColumn)
            if (relatives.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "قائمة فارغة",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "لا توجد نتائج بحث مطابقة",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "تأكد من كتابة الاسم أو الرقم بشكل صحيح أو اضف قريباً جديداً",
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(relatives) { relative ->
                    RelativeCard(relative = relative, viewModel = viewModel)
                }
            }
        }
    }

    // Modal Components
    AddRelativeDialog(viewModel)
    ImportContactsDialog(viewModel)
    RecordLogDialog(viewModel)
    LogsHistoryDialog(viewModel) // Handcrafted Profile & Dotted Timeline view!
    QuickTemplatesDialog(viewModel)
    SetReminderDialog(viewModel)
    SettingsDialog(viewModel)
}

@Composable
fun RelativeCard(relative: Relative, viewModel: RelativeViewModel) {
    val status = viewModel.getRelativeStatus(relative)
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Select dynamic soft pastel container and border color based on relationship degree
    val (containerColor, borderColor) = when (relative.relationshipDegree) {
        "والدان" -> Pair(Color(0xFFFFFDF2), Color(0xFFF3EAC2))       // Warm Gold Pastel
        "أشقاء" -> Pair(Color(0xFFF4FAF6), Color(0xFFD6ECE0))        // Soft Emerald/Mint Pastel
        "أعمام/أخوال" -> Pair(Color(0xFFF2F9FA), Color(0xFFD2EFF1))  // Muted Blue-Teal Pastel
        else -> Pair(Color.White, Color(0xFFEEEDE7))                // Clean Pure Soft White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("relative_card_${relative.id}")
            .clickable { showMenu = !showMenu }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = borderColor.copy(alpha = 0.5f),
                spotColor = borderColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = relative.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(borderColor.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = relative.relationshipDegree,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = relative.phone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    // Status tag
                    val statusBgColor = Color(android.graphics.Color.parseColor("#1C" + status.colorHex))
                    val statusTextColor = Color(android.graphics.Color.parseColor("#FF" + status.colorHex))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBgColor)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = status.label,
                            color = statusTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // WhatsApp Contact Button
                    Button(
                        onClick = {
                            var cleanPhone = relative.phone.replace("\\s|-|\\(|\\)".toRegex(), "")
                            if (!cleanPhone.startsWith("+") && !cleanPhone.startsWith("00")) {
                                if (cleanPhone.startsWith("0")) {
                                    cleanPhone = "966" + cleanPhone.substring(1)
                                }
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                            }
                            context.startActivity(intent)
                            viewModel.recordCommunication(relative.id, "رسالة", "تواصل سريع عبر واتساب")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("whatsapp_connect_button_${relative.id}"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "واتساب",
                            modifier = Modifier.size(13.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("واتساب", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "آخر تواصل: ${formatRelativeDate(relative.lastContactDate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "كل ${relative.contactIntervalDays} يوم",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (relative.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ملاحظة: ${relative.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // Quick interaction actions drawer
            AnimatedVisibility(
                visible = showMenu,
                enter = expandVertically(animationSpec = spring()) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring()) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = borderColor.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Record Log button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.showRecordLogDialog.value = relative }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "تسجيل صلة",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("تسجيل صِلة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // 2. Templates button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.showQuickTemplatesDialog.value = relative }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF9E6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "قوالب رسائل",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("رسائل جاهزة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF947214))
                        }

                        // 3. Profile & Timeline (Historical Logs) button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.showLogsHistoryDialog.value = relative }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle, // Changed to AccountCircle for Profile look
                                    contentDescription = "الملف الشخصي",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الملف الكامل", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }

                        // 4. Set Reminder button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.showSetReminderDialog.value = relative }
                                .padding(4.dp)
                                .testTag("set_reminder_button_${relative.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3E8FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "التذكير",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ضبط التذكير", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                        }

                        // 5. Delete button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.deleteRelative(relative) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("حذف القريب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Add Relative manually
@Composable
fun AddRelativeDialog(viewModel: RelativeViewModel) {
    val show by viewModel.showAddRelativeDialog.collectAsState()
    if (!show) return

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("والدان") }
    var interval by remember { mutableStateOf("7") }
    var notes by remember { mutableStateOf("") }

    val degrees = listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")

    Dialog(onDismissRequest = { viewModel.showAddRelativeDialog.value = false }) {
        Card(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.2f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, Color(0xFFE9CE79).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "إضافَةُ قَرِيبٍ جَدِيد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم الكامل للقريب") },
                    placeholder = { Text("مثال: الجدّ أبو أحمد") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_relative_name_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    placeholder = { Text("مثال: 05xxxxxxx") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_relative_phone_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Relationship Degree selection
                Text(
                    text = "صلة القرابة:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    degrees.take(2).forEach { deg ->
                        val isSelected = degree == deg
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFEEEDE7)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { degree = deg }
                        ) {
                            Text(
                                text = deg,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    degrees.drop(2).forEach { deg ->
                        val isSelected = degree == deg
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFEEEDE7)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { degree = deg }
                        ) {
                            Text(
                                text = deg,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interval Input
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it },
                    label = { Text("تكرار التذكير بالصلة (بالأيام)") },
                    placeholder = { Text("مثال: 7") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية (اختياري)") },
                    placeholder = { Text("مثال: يفضل الاتصال به مساءً") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.showAddRelativeDialog.value = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                viewModel.addRelative(
                                    name = name,
                                    phone = phone,
                                    relationshipDegree = degree,
                                    intervalDays = interval.toIntOrNull() ?: 7,
                                    notes = notes
                                )
                                viewModel.showAddRelativeDialog.value = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_relative_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("حفظ القريب", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog: Import Contacts list from phone book with Google Sync
@Composable
fun ImportContactsDialog(viewModel: RelativeViewModel) {
    val show by viewModel.showImportContactsDialog.collectAsState()
    if (!show) return

    val contacts by viewModel.deviceContacts.collectAsState()
    val isLoading by viewModel.isLoadingContacts.collectAsState()
    var searchContactText by remember { mutableStateOf("") }
    
    // Google filter toggle: 0 for all, 1 for Google contacts only
    var selectedFilterTab by remember { mutableStateOf(0) }

    val filteredContacts = contacts.filter { contact ->
        val matchesSearch = contact.first.contains(searchContactText, ignoreCase = true) || contact.second.contains(searchContactText)
        val matchesFilter = if (selectedFilterTab == 1) contact.third else true
        matchesSearch && matchesFilter
    }

    Dialog(onDismissRequest = { viewModel.showImportContactsDialog.value = false }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.15f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "استيراد جهات الاتصال",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "استورد الأقارب من الهاتف أو حساب Google مباشرة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Filter tabs (All / Google Contacts)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEEDE7).copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("الكل", "جهات اتصال Google ☁️").forEachIndexed { index, title ->
                        val isSelected = selectedFilterTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { selectedFilterTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchContactText,
                    onValueChange = { searchContactText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ابحث بالاسم أو رقم الهاتف...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.secondary) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // List Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(20.dp))
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (filteredContacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لا توجد جهات اتصال مطابقة",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedFilterTab == 1) "تأكد من وجود جهات اتصال مخزنة في حساب Google المتزامن مع جهازك." else "لم نجد أي جهة اتصال بالاسم المطلوب.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            items(filteredContacts) { contact ->
                                var showDegreeSelector by remember { mutableStateOf(false) }

                                Column {
                                    val suggestedDegree = viewModel.suggestRelationshipDegree(contact.first)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showDegreeSelector = !showDegreeSelector }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Contact Initials Avatar
                                        val initialChar = contact.first.trim().firstOrNull()?.toString() ?: "ق"
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.background)
                                                .border(1.dp, Color(0xFFE9CE79).copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Text(
                                                text = initialChar,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Info column
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = contact.first,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                if (contact.third) {
                                                    // Google Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFE0F2FE))
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Google ☁️",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF0369A1)
                                                        )
                                                    }
                                                }
                                                // Suggested Relationship Badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFE2ECC8))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "صلة مقترحة: $suggestedDegree ✨",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF3F694D)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = contact.second,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Quick Smart Save Button
                                        IconButton(
                                            onClick = {
                                                viewModel.addRelative(
                                                    name = contact.first,
                                                    phone = contact.second,
                                                    relationshipDegree = suggestedDegree,
                                                    intervalDays = if (suggestedDegree == "والدان") 1 else if (suggestedDegree == "أشقاء") 7 else 14,
                                                    notes = "حفظ ذكي - تم التعرف التلقائي على درجة القرابة ($suggestedDegree) من الهاتف"
                                                )
                                                viewModel.showImportContactsDialog.value = false
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color(0xFFD5BE72).copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "حفظ ذكي سريع",
                                                tint = Color(0xFF9E842D),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = if (showDegreeSelector) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "خيارات الإضافة",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = showDegreeSelector,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Column(modifier = Modifier.padding(bottom = 10.dp, start = 4.dp, end = 4.dp)) {
                                            Text(
                                                text = "اختر درجة القرابة للحفظ الفوري:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون").forEach { deg ->
                                                    Card(
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.dp, Color(0xFFE9CE79).copy(alpha = 0.5f)),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                viewModel.addRelative(
                                                                    name = contact.first,
                                                                    phone = contact.second,
                                                                    relationshipDegree = deg,
                                                                    intervalDays = if (deg == "والدان") 1 else if (deg == "أشقاء") 7 else 14,
                                                                    notes = "تم الاستيراد من الهاتف" + (if (contact.third) " (حساب Google)" else "")
                                                                )
                                                                viewModel.showImportContactsDialog.value = false
                                                            }
                                                    ) {
                                                        Text(
                                                            text = deg,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 8.dp),
                                                            textAlign = TextAlign.Center,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFEEEDE7).copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.showImportContactsDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("إغلاق النافذة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Dialog: Record communication log
@Composable
fun RecordLogDialog(viewModel: RelativeViewModel) {
    val relative by viewModel.showRecordLogDialog.collectAsState()
    if (relative == null) return

    var selectedType by remember { mutableStateOf("اتصال") }
    var logNotes by remember { mutableStateOf("") }
    val types = listOf("اتصال", "رسالة", "زيارة")

    Dialog(onDismissRequest = { viewModel.showRecordLogDialog.value = null }) {
        Card(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.2f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, Color(0xFFE9CE79).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "تسجيل صلة تواصل",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "سجل تفاصيل وصلك وتواصلك مع القريب: ${relative?.name}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 14.dp),
                    lineHeight = 18.sp
                )

                // Type selection with icons
                Text(
                    text = "طريقة التواصل:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = selectedType == type
                        val (bgColor, iconColor, icon) = when (type) {
                            "زيارة" -> Triple(if (isSelected) MaterialTheme.colorScheme.primary else Color.White, if (isSelected) Color.White else Color(0xFFD4AF37), Icons.Default.Home)
                            "اتصال" -> Triple(if (isSelected) MaterialTheme.colorScheme.primary else Color.White, if (isSelected) Color.White else Color(0xFF0284C7), Icons.Default.Phone)
                            "رسالة" -> Triple(if (isSelected) MaterialTheme.colorScheme.primary else Color.White, if (isSelected) Color.White else Color(0xFF15803D), Icons.Default.Chat)
                            else -> Triple(if (isSelected) MaterialTheme.colorScheme.primary else Color.White, if (isSelected) Color.White else MaterialTheme.colorScheme.secondary, Icons.Default.Assignment)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFEEEDE7)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = type,
                                    tint = iconColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes input area
                OutlinedTextField(
                    value = logNotes,
                    onValueChange = { logNotes = it },
                    label = { Text("تفاصيل التواصل أو المناسبة") },
                    placeholder = { Text("مثال: زيارة في منزله والاطمئنان على صحته") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.showRecordLogDialog.value = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            relative?.let { rel ->
                                viewModel.recordCommunication(
                                    relativeId = rel.id,
                                    type = selectedType,
                                    notes = logNotes
                                )
                            }
                            viewModel.showRecordLogDialog.value = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("تسجيل الصلة ✨", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Dialog: View Logs history for a relative
@Composable
fun LogsHistoryDialog(viewModel: RelativeViewModel) {
    val relative by viewModel.showLogsHistoryDialog.collectAsState()
    if (relative == null) return

    val logs by viewModel.logs.collectAsState()
    val relativeLogs = logs.filter { it.relativeId == relative?.id }.sortedByDescending { it.timestamp }
    val context = LocalContext.current

    // Generate Arabic Initials
    val initials = relative?.name?.trim()?.split("\\s+".toRegex())
        ?.filter { it.isNotEmpty() }
        ?.map { it.first() }
        ?.joinToString("")
        ?.take(2) ?: "ق"

    Dialog(onDismissRequest = { viewModel.showLogsHistoryDialog.value = null }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background), // Adaptable Tint
            border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Profile Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar emblem
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFE9CE79).copy(alpha = 0.3f), Color(0xFFE9CE79).copy(alpha = 0.05f))
                                )
                            )
                            .border(1.5.dp, Color(0xFFE9CE79), CircleShape)
                    ) {
                        Text(
                            text = initials,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = relative?.name ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = relative?.phone ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = relative?.relationshipDegree ?: "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("تكرار الصلة", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("كل ${relative?.contactIntervalDays} يوم", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("إجمالي التواصل", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${relativeLogs.size} تواصل", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var activeSubTab by remember { mutableStateOf(0) } // 0 for Logs, 1 for Time Capsule
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEEEDE7).copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("سِجلُّ المَوَدَّةِ", "كبسولة الزمن ⏳").forEachIndexed { index, title ->
                        val isSelected = activeSubTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { activeSubTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeline container
                Box(modifier = Modifier.weight(1f)) {
                    if (activeSubTab == 0) {
                    if (relativeLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "لا توجد نشاطات مسجلة بعد",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "بادر بالتواصل وسجّل مروءتك وصلتك هنا لتزدهر مسيرتك العائلية ✨",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            itemsIndexed(relativeLogs) { index, log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Left side: Connection activity content
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = log.type,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp
                                            )
                                            val formattedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(log.timestamp))
                                            Text(
                                                text = formattedDate,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        if (log.notes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = log.notes,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (index < relativeLogs.size - 1) {
                                            HorizontalDivider(color = Color(0xFFEEEDE7).copy(alpha = 0.6f))
                                        }
                                    }

                                    // Right side: Custom aesthetic timeline node
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(36.dp)
                                    ) {
                                        // Dynamic warm color and icon based on log type
                                        val (nodeBgColor, nodeIconColor, nodeIcon) = when (log.type) {
                                            "زيارة" -> Triple(Color(0xFFFFF9E6), Color(0xFFD4AF37), Icons.Default.Home)
                                            "مكالمة" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Default.Phone)
                                            "رسالة" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.Chat)
                                            else -> Triple(Color(0xFFF3E8FF), Color(0xFF7C3AED), Icons.Default.Assignment)
                                        }

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(nodeBgColor)
                                                .border(1.dp, nodeIconColor.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = nodeIcon,
                                                contentDescription = null,
                                                tint = nodeIconColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        if (index < relativeLogs.size - 1) {
                                            Canvas(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .weight(1f)
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                drawLine(
                                                    color = Color(0xFFE9CE79).copy(alpha = 0.4f),
                                                    start = androidx.compose.ui.geometry.Offset(x = size.width / 2, y = 0f),
                                                    end = androidx.compose.ui.geometry.Offset(x = size.width / 2, y = size.height),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    } else {
                        // FAMILY TIME CAPSULE (MEMORIES)
                        val memoriesList by viewModel.memories.collectAsState()
                        val relativeMemories = memoriesList.filter { it.relativeId == relative?.id }
                        var showAddMemoryForm by remember { mutableStateOf(false) }
                        
                        var memoryTitle by remember { mutableStateOf("") }
                        var memoryDesc by remember { mutableStateOf("") }

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (!showAddMemoryForm) {
                                Button(
                                    onClick = { showAddMemoryForm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF9E6)),
                                    border = BorderStroke(1.dp, Color(0xFFE9CE79)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تسجيل لحظة غالية جديدة ✨", color = Color(0xFFB45309), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE9CE79).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F5)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("أرشفة ذكرى مميزة ⏳", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB45309))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = memoryTitle,
                                            onValueChange = { memoryTitle = it },
                                            placeholder = { Text("عنوان الذكرى (مثال: زيارة العيد البهيجة)", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = memoryDesc,
                                            onValueChange = { memoryDesc = it },
                                            placeholder = { Text("تفاصيل اللحظة والمشاعر الجميلة...", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (memoryTitle.isNotBlank()) {
                                                        viewModel.addFamilyMemory(
                                                            relativeId = relative?.id ?: 0,
                                                            relativeName = relative?.name ?: "",
                                                            title = memoryTitle,
                                                            description = memoryDesc
                                                        )
                                                        memoryTitle = ""
                                                        memoryDesc = ""
                                                        showAddMemoryForm = false
                                                    }
                                                },
                                                modifier = Modifier.weight(1.5f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("حفظ وتأمين", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            OutlinedButton(
                                                onClick = { showAddMemoryForm = false },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("إلغاء", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (relativeMemories.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFD5BE72),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("كبسولة الزمن العائلية فارغة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("سجل اللحظات السعيدة، الصور، أو المناسبات الخاصة لتعود لك كذكرى مفاجئة دافئة بعد مرور عام ✨", fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(20.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(relativeMemories) { memory ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F5)),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE9CE79).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFFBEB)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.HourglassEmpty,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD5BE72),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(memory.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                                    if (memory.description.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(memory.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 16.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    val memDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(memory.timestamp))
                                                    Text("تم الأرشفة في: $memDate", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteFamilyMemory(memory) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف الذكرى",
                                                        tint = Color.Red.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.showLogsHistoryDialog.value = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("إغلاق الملف الشخصي", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Dialog: Share Quick templates with relative
@Composable
fun QuickTemplatesDialog(viewModel: RelativeViewModel) {
    val relative by viewModel.showQuickTemplatesDialog.collectAsState()
    if (relative == null) return

    val templates by viewModel.templates.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = { viewModel.showQuickTemplatesDialog.value = null }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = 0.15f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Profile Card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "رسائل التواصل الجاهزة",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "اختر رسالة مودة لـ: ${relative?.name}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Templates List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(templates) { template ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFEEEDE7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = template.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = template.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = template.content,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Copy Button
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            val clip = android.content.ClipData.newPlainText("quick_message", template.content)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "تم نسخ نص الرسالة بنجاح 📋", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "نسخ",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text("نسخ الرسالة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    // Send/Share Button
                                    Button(
                                        onClick = {
                                            relative?.let { rel ->
                                                viewModel.recordCommunication(rel.id, "رسالة", "قالب جاهز: ${template.title}")

                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, template.content)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "أرسل التهنئة عبر:"))
                                            }
                                            viewModel.showQuickTemplatesDialog.value = null
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "إرسال",
                                                modifier = Modifier.size(14.dp),
                                                tint = Color.White
                                            )
                                            Text("مشاركة وإرسال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.showQuickTemplatesDialog.value = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C8A82)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("إغلاق النافذة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SetReminderDialog(viewModel: RelativeViewModel) {
    val relative by viewModel.showSetReminderDialog.collectAsState()
    if (relative == null) return

    val context = LocalContext.current
    var intervalStr by remember { mutableStateOf(relative?.contactIntervalDays?.toString() ?: "7") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "الرجاء تفعيل الإشعارات لتلقي التذكيرات في موعدها", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(relative) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Dialog(onDismissRequest = { viewModel.showSetReminderDialog.value = null }) {
        Card(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.15f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ضبط تذكير التواصل",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "للقريب: ${relative?.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "سيقوم التطبيق بإرسال إشعارات تذكيرية لك تلقائياً حسب الفترة الزمنية التي تحددها بالأيام لتبقي حبال الود متصلة دائماً.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = intervalStr,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            intervalStr = it
                            errorMessage = null
                        }
                    },
                    label = { Text("فترة التذكير (بالأيام)", fontSize = 12.sp) },
                    suffix = { Text("أيام", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_interval_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFEEEDE7)
                    ),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.showSetReminderDialog.value = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = {
                            val days = intervalStr.toIntOrNull()
                            if (days == null || days <= 0) {
                                errorMessage = "الرجاء إدخال عدد أيام صحيح أكبر من 0"
                                return@Button
                            }
                            relative?.let { rel ->
                                viewModel.updateRelativeInterval(rel, days)
                                Toast.makeText(context, "تم ضبط التذكير ليكون كل $days يوم بنجاح 🎉", Toast.LENGTH_LONG).show()
                            }
                            viewModel.showSetReminderDialog.value = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("save_reminder_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("حفظ وتفعيل ✨", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengesTab(viewModel: RelativeViewModel) {
    val relatives by viewModel.relatives.collectAsState()
    val logs by viewModel.logs.collectAsState()

    // Calculate gamification statistics
    val totalRelatives = relatives.size
    val contactedThisMonth = relatives.count { relative ->
        relative.lastContactDate > 0L && (System.currentTimeMillis() - relative.lastContactDate) <= (1000L * 60 * 60 * 24 * 30)
    }

    // High Priority contacts
    val urgentContacts = relatives.filter {
        val status = viewModel.getRelativeStatus(it)
        status == RelativeStatus.NEEDS_CONTACT_URGENT || status == RelativeStatus.OVERDUE_CRITICAL || status == RelativeStatus.NEEDS_CONTACT
    }

    // Coverage percentage
    val percentage = if (totalRelatives > 0) (contactedThisMonth.toFloat() / totalRelatives.toFloat() * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card: Progress Gauge
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نسبة صلة الرحم هذا الشهر",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0x22FFFFFF),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10.dp.toPx())
                            )
                            drawArc(
                                color = Color(0xFFE9CE79),
                                startAngle = -90f,
                                sweepAngle = (percentage * 3.6).toFloat(),
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$percentage%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "تغطية التواصل",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9CE79)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "عن أنس رضي الله عنه أن رسول الله ﷺ قال:\n\"من أحب أن يبسط له في رزقه، وينسأ له في أثره، فليصل رحمه.\"",
                        fontSize = 11.sp,
                        color = Color(0xFFFAF9F5),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Stats Row Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "إجمالي الأقارب",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalRelatives",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تواصل هذا الشهر",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$contactedThisMonth",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }

        // Weekly Challenge Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "تحدي الأسبوع",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "التحدي الأسبوعي الجاري 🏆",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "قم بزيارة أو الاتصال بـ 3 من الأعمام أو الأخوال قبل نهاية يوم الجمعة لتأكيد صلة رحمك وصنع البسمة على وجوههم.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Urgent Actions Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚠️", fontSize = 12.sp)
                }
                Text(
                    text = "يحتاجون صلة عاجلة",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (urgentContacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Text(
                        text = "ما شاء الله! كل أقاربك متصلون بشكل كافٍ وجدولك عامر بالود والصلة. 💚",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }
            }
        } else {
            items(urgentContacts.take(4)) { relative ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showRecordLogDialog.value = relative },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = relative.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تأخر تواصله: ${formatRelativeDate(relative.lastContactDate)}",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = { viewModel.showRecordLogDialog.value = relative },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("اتصل الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
 fun TemplatesTab(viewModel: RelativeViewModel) {
    val templates by viewModel.templates.collectAsState()
    var showAddTemplate by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("تهنئة") }
    
    // --- SMART FEATURE 2: Local Smart Message Generator State ---
    var smartRelativeName by remember { mutableStateOf("") }
    var smartKinshipDegree by remember { mutableStateOf("والدان") }
    var smartOccasion by remember { mutableStateOf("سؤال عام وتفقد") }
    var smartGeneratedResult by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTemplate = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة قالب مخصص")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "مكتبة القوالب والرسائل",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "مجموعة من الرسائل الجاهزة لمشاركتها فوراً أو نسخها لإرسالها لأقاربك في المناسبات والأيام المباركة.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                lineHeight = 16.sp
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // LOCAL SMART MESSAGE GENERATOR CARD
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFFE9CE79).copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F5)),
                        border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFFBEB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFD5BE72),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "مُوَلِّدُ رَسَائِلِ الوَصْلِ الذَّكِيَّةِ ✨",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "توليد رسائل ودّية تلامس القلوب بناءً على المناسبة",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Name input
                            OutlinedTextField(
                                value = smartRelativeName,
                                onValueChange = { smartRelativeName = it },
                                placeholder = { Text("اسم القريب (مثال: عمتي سارة، عمي أحمد)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Selectors row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Kinship dropdown/chips simulated
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("درجة القرابة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(10.dp))
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        listOf("والدان", "أشقاء", "أعمام", "آخر").forEach { k ->
                                            val isSel = when (k) {
                                                "والدان" -> smartKinshipDegree == "والدان"
                                                "أشقاء" -> smartKinshipDegree == "أشقاء"
                                                "أعمام" -> smartKinshipDegree == "أعمام/أخوال"
                                                else -> smartKinshipDegree == "أقارب آخرون"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                    .clickable {
                                                        smartKinshipDegree = when (k) {
                                                            "والدان" -> "والدان"
                                                            "أشقاء" -> "أشقاء"
                                                            "أعمام" -> "أعمام/أخوال"
                                                            else -> "أقارب آخرون"
                                                        }
                                                    }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(k, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Occasion row
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("المناسبة واللحظة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFEEEDE7), RoundedCornerShape(10.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    listOf("يوم الجمعة", "الأعياد", "سؤال عام", "شفاء").forEach { o ->
                                        val isSel = when (o) {
                                            "يوم الجمعة" -> smartOccasion == "يوم الجمعة"
                                            "الأعياد" -> smartOccasion == "الأعياد المباركة"
                                            "سؤال عام" -> smartOccasion == "سؤال عام وتفقد"
                                            else -> smartOccasion == "دعاء بالشفاء"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) Color(0xFF436F51) else Color.Transparent)
                                                .clickable {
                                                    smartOccasion = when (o) {
                                                        "يوم الجمعة" -> "يوم الجمعة"
                                                        "الأعياد" -> "الأعياد المباركة"
                                                        "سؤال عام" -> "سؤال عام وتفقد"
                                                        else -> "دعاء بالشفاء"
                                                    }
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(o, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Generate Button
                            Button(
                                onClick = {
                                    val cleanName = if (smartRelativeName.trim().isEmpty()) "الغالي" else smartRelativeName.trim()
                                    smartGeneratedResult = when (smartOccasion) {
                                        "يوم الجمعة" -> {
                                            when (smartKinshipDegree) {
                                                "والدان" -> "أبي وأمي العزيزين، في هذه الجمعة المباركة أبعث لكم بأصدق الدعوات، سائلاً المولى أن يحفظكم ويطيل عمركم في طاعته ويرزقني رضاكم وبركم المبارك 🌸."
                                                "أشقاء" -> "أخي الغالي $cleanName، جمعتك مباركة وطيبة. أسأل الله أن يبارك في عمرك وصحتك ويجمعنا دائماً على المودة والمحبة والخير الدائم ✨."
                                                "أعمام/أخوال" -> "عمي العزيز $cleanName، يوم جمعة مبارك وطيب أرجوه لك ولعائلتك الكريمة. حفظك الله وبارك في صحتك وعافيتك ودمت ذخراً وفخراً لنا 🤍."
                                                else -> "قريبي العزيز $cleanName، في هذا اليوم الطيب المبارك، أتمنى لك جمعة عامرة برضوان الله وعفوه، ملؤها الطمأنينة والخير الوفير 🌟."
                                            }
                                        }
                                        "الأعياد المباركة" -> {
                                            when (smartKinshipDegree) {
                                                "والدان" -> "عيدكم مبارك يا مهجة قلبي ومصدر فرحتي، كل عام وأنتم تاج رأسي وبصحة وعافية، تقبل الله منا ومنكم صالح الأعمال والبركات 🌸."
                                                "أشقاء" -> "أخي الحبيب $cleanName، عيدك مبارك وسعيد. أدام الله فرحتنا في العائلة وجمع شملنا على المودة واليسر والبركة والمسرات العذبة 💚."
                                                "أعمام/أخوال" -> "عمي الغالي $cleanName، عيدكم مبارك وكل عام وأنتم بأتم صحة وعافية، أعاده الله عليكم باليمن والبركات والسرور المبارك 🤍."
                                                else -> "قريبي الغالي $cleanName، كل عام وأنت بخير بمناسبة العيد السعيد، أسأل الله أن يتقبل طاعاتك ويعيده علينا وعليك بالمسرات والهناء الجميل ✨."
                                            }
                                        }
                                        "سؤال عام وتفقد" -> {
                                            when (smartKinshipDegree) {
                                                "والدان" -> "والدي العزيزين، أردت تفقد أحوالكم والدعاء لكم بالسلامة، رضاكم هو غايتي ودعواتكم هي سر توفيقي وبركتي في هذه الحياة الغالية 🌸."
                                                "أشقاء" -> "أخي الغالي $cleanName، أرجو أن تكون بأفضل حال وصحة وسعادة. تفقدت أحوالك شوقاً لحديثك الطيب، وأتمنى لك يوماً موفقاً في طاعة الله 💚."
                                                "أعمام/أخوال" -> "عمي العزيز $cleanName، أرسل لك هذه الكلمات لأطمئن على صحتك الغالية، أرجو من الله أن تكون في أحسن حال وراحة بال وسلامة تامة 🤍."
                                                else -> "قريبي العزيز $cleanName، أردت السلام عليك وتفقد أحوالك الكريمة، دمت في حفظ الله ورعايته دائماً وأبداً وأسعد الله قلبك الطاهر ✨."
                                            }
                                        }
                                        else -> {
                                            when (smartKinshipDegree) {
                                                "والدان" -> "اللهم اشفِ والديّ وعافهم، وألبسهم ثوب الصحة والعافية الطهورة الشافية التي لا تغادر سقماً، واحفظهم لنا يا مجيب الدعاء 🌸."
                                                "أشقاء" -> "أخي العزيز $cleanName، أسأل الله العظيم رب العرش العظيم أن يشفيك ويعافيك، طهور ونور إن شاء الله يا حبيب القلب وصديق العمر 💚."
                                                "أعمام/أخوال" -> "عمي الحبيب $cleanName، شفاك الله وعافاك وخفف عنك كل ألم وجعله كفارة لك، أسأل الله أن يمنّ عليك بتمام الصحة عاجلاً غير آجل 🤍."
                                                else -> "قريبي الغالي $cleanName، طهور إن شاء الله، أسأل الله الشافي الكافي أن يشفيك ويعافيك من كل سوء ومكروه ويلبسك ثوب الصحة والعافية ✨."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("توليد نص الرسالة الذكية ✨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            if (smartGeneratedResult.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE9CE79).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = smartGeneratedResult,
                                            fontSize = 12.sp,
                                            lineHeight = 20.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Right
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("Sila message", smartGeneratedResult)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "تم نسخ الرسالة الذكية بنجاح!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFBEB))
                                            ) {
                                                Text("نسخ الرسالة 📋", color = Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, smartGeneratedResult)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "مشاركة الرسالة عبر"))
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9))
                                            ) {
                                                Text("مشاركة فورية 📲", color = Color(0xFF1B5E20), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                items(templates) { template ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEDE7))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = template.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = template.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = template.content,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Copy Text Button
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("quick_message", template.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ نص الرسالة بنجاح 📋", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFEEEDE7)),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "نسخ نص الرسالة بالكامل",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTemplate) {
        Dialog(onDismissRequest = { showAddTemplate = false }) {
            Card(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .shadow(16.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.15f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.5.dp, Color(0xFFE9CE79).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "إضافة قالب رسالة جديد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان القالب (مثال: تهنئة بالنجاح)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFEEEDE7)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("محتوى الرسالة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFEEEDE7)
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف (مثال: مناسبات)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFEEEDE7)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddTemplate = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (title.isNotEmpty() && content.isNotEmpty()) {
                                    viewModel.insertTemplate(
                                        QuickTemplate(title = title, content = content, category = category)
                                    )
                                    showAddTemplate = false
                                    title = ""
                                    content = ""
                                    category = "تهنئة"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("حفظ القالب ✨", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(viewModel: RelativeViewModel) {
    val show by viewModel.showSettingsDialog.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    if (!show) return

    Dialog(onDismissRequest = { viewModel.showSettingsDialog.value = false }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.12f)),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) MaterialTheme.colorScheme.surface else Color(0xFFFAF9F5)
            ),
            border = BorderStroke(1.5.dp, if (isDarkMode) Color.DarkGray.copy(alpha = 0.5f) else Color(0xFFE9CE79).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with settings icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "الإعدادات العامة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "تخصيص مظهر التطبيق وخياراته",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Settings Item: Dark Mode Toggle
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF252C28) else Color.White
                    ),
                    border = BorderStroke(1.1.dp, if (isDarkMode) Color.DarkGray.copy(alpha = 0.3f) else Color(0xFFEEEDE7)),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDarkMode) Color(0x1AE9CE79) else Color(0xFFFFFBEB)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFFE9CE79) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "المظهر الداكن",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "تفعيل المظهر الداكن لتصفح مريح للعين",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // تخصيص الخطوط العربية (Dynamic Arabic Typography)
                val selectedFont by viewModel.selectedFont.collectAsState()

                Text(
                    text = "خط التطبيق (العربي)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val fontOptions = listOf(
                    Triple("Thamanyah", "خط ثمانية (Thamanyah OS)", "خيار مميز يعطي طابعاً ثقافياً وعصرياً فريداً"),
                    Triple("Cairo", "خط القاهرة (Cairo)", "خط كلاسيكي حديث ومنسجم للواجهات والفقرات"),
                    Triple("Almarai", "خط المراعي (Almarai)", "خط ناعم يتناسب مع الواجهات اللطيفة والمتوازنة"),
                    Triple("Tajawal", "خط تجوال (Tajawal)", "خط هندسي متميز وواضح للقراءة السريعة والمنسقة")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    fontOptions.forEach { (key, label, desc) ->
                        val isSelected = selectedFont == key
                        val fontFam = remember(key) { getFontFamily(key) }
                        
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    if (isDarkMode) Color(0xFF252C28) else Color.White
                                }
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.8.dp else 1.1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else (if (isDarkMode) Color.DarkGray.copy(alpha = 0.3f) else Color(0xFFEEEDE7))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectFont(key) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        fontFamily = fontFam,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontFamily = fontFam,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // Extra informative card (Soft UI styled)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1B221E) else Color(0xFFFAF9F5)
                    ),
                    border = BorderStroke(1.dp, if (isDarkMode) Color.DarkGray.copy(alpha = 0.2f) else Color(0xFFE9CE79).copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "تطبيق صلة الرحم ✨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الإصدار 1.2.0 - صُنع لتعزيز أواصر المودة والتواصل مع الأقارب والوالدين.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Button(
                    onClick = { viewModel.showSettingsDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "إغلاق النافذة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

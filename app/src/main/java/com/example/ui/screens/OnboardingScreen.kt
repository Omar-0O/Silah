package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.RelativeViewModel

data class OnboardingItem(
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val illustrationEmoji: String = ""
)

@Composable
fun OnboardingScreen(
    viewModel: RelativeViewModel,
    onFinished: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val savedName by viewModel.userName.collectAsState()
    val savedGender by viewModel.userGender.collectAsState()
    val showImportContactsDialog by viewModel.showImportContactsDialog.collectAsState()

    var nameInput by remember { mutableStateOf(savedName) }
    var genderInput by remember { mutableStateOf(savedGender) }
    var currentPage by remember { mutableIntStateOf(0) }
    var showNameError by remember { mutableStateOf(false) }
    var showRelativeError by remember { mutableStateOf(false) }

    val pages = 4

    // Main scaffold
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top teal header ────────────────────────────────────────────────
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "header"
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0A5C60), Color(0xFF0E7075), Color(0xFF1A9499))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        // Logo / emoji illustration
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            when (page) {
                                0 -> androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                                1 -> Text("✏️", fontSize = 28.sp)
                                2 -> Text("📱", fontSize = 28.sp)
                                else -> Text("🌿", fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = when (page) {
                                0 -> if (selectedLanguage == "en") "Welcome to Silah 🌿" else "مرحباً بك في صِلَةِ 🌿"
                                1 -> if (selectedLanguage == "en") "Tell Us About You" else "أخبرنا عن نفسك"
                                2 -> if (selectedLanguage == "en") "Add Your Relatives" else "أضف أرحامك"
                                else -> if (selectedLanguage == "en") "All Set! 🎉" else "كل شيء جاهز! 🎉"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (page) {
                                0 -> if (selectedLanguage == "en")
                                    "Your private companion for nurturing family ties — no ads, no subscriptions."
                                else
                                    "رفيقك الخاص في العناية بأرحامك وأقاربك — بدون إعلانات أو اشتراكات."
                                1 -> if (selectedLanguage == "en")
                                    "We'll personalize your reminders with your name."
                                else
                                    "عشان نخصّص التذكيرات ليك باسمك."
                                2 -> if (selectedLanguage == "en")
                                    "Import family members directly from your phone contacts."
                                else
                                    "استورد أفراد عائلتك مباشرة من جهات اتصالك."
                                else -> if (selectedLanguage == "en")
                                    "Silah will remind you and track your connections automatically."
                                else
                                    "صِلَةِ هيذكّرك ويتابع تواصلك تلقائياً."
                            },
                            fontSize = 12.sp,
                            color = Color(0xFFB8DDE0),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ── Page content ───────────────────────────────────────────────────
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        // Forward: new page enters from RIGHT, old exits to LEFT
                        slideInHorizontally(tween(300)) { it } + fadeIn(tween(200)) togetherWith
                                slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
                    } else {
                        // Back: new page enters from LEFT, old exits to RIGHT
                        slideInHorizontally(tween(300)) { -it } + fadeIn(tween(200)) togetherWith
                                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
                    }
                },
                label = "page_content",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Content for each page
                    Box(modifier = Modifier.weight(1f)) {
                        when (page) {
                            // ── Page 0: Language ─────────────────────────────
                            0 -> LanguagePage(selectedLanguage, viewModel)
                            // ── Page 1: Name & Gender ────────────────────────
                            1 -> NameGenderPage(
                                nameInput = nameInput,
                                genderInput = genderInput,
                                showNameError = showNameError,
                                selectedLanguage = selectedLanguage,
                                onNameChange = {
                                    nameInput = it
                                    if (it.trim().isNotEmpty()) showNameError = false
                                },
                                onGenderChange = { genderInput = it }
                            )
                            // ── Page 2: Import contacts ───────────────────────
                            2 -> ImportRelativesPage(
                                viewModel = viewModel,
                                selectedLanguage = selectedLanguage,
                                showRelativeError = showRelativeError
                            )
                            // ── Page 3: Summary ───────────────────────────────
                            else -> SummaryPage(selectedLanguage)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Dots + Navigation ──────────────────────────────────────
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Dot indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(pages) { index ->
                                val isActive = index == page
                                Box(
                                    modifier = Modifier
                                        .animateContentSize()
                                        .height(8.dp)
                                        .width(if (isActive) 24.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) Color(0xFF0E7075) else Color(0xFFCBD5E1)
                                        )
                                )
                            }
                        }

                        // Navigation buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back button
                            if (page > 0) {
                                OutlinedButton(
                                    onClick = { currentPage-- },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.size(50.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Next / Finish button
                            Button(
                                onClick = {
                                    when (page) {
                                        1 -> {
                                            if (nameInput.trim().isEmpty()) {
                                                showNameError = true
                                                return@Button
                                            }
                                            viewModel.saveUserProfile(nameInput.trim(), genderInput)
                                            currentPage++
                                        }
                                        2 -> {
                                            if (viewModel.relatives.value.isEmpty()) {
                                                showRelativeError = true
                                                return@Button
                                            }
                                            showRelativeError = false
                                            currentPage++
                                        }
                                        pages - 1 -> {
                                            viewModel.showImportContactsDialog.value = false
                                            onFinished()
                                        }
                                        else -> currentPage++
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0E7075),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = if (page < pages - 1) {
                                        if (selectedLanguage == "en") "Continue" else "التالي"
                                    } else {
                                        if (selectedLanguage == "en") "Get Started 🌸" else "ابدأ الآن 🌸"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (page < pages - 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
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

    if (showImportContactsDialog) {
        com.example.ui.dialogs.ImportContactsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showImportContactsDialog.value = false }
        )
    }
}

// ── Language Selection Page ─────────────────────────────────────────────────
@Composable
private fun LanguagePage(selectedLanguage: String, viewModel: RelativeViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (selectedLanguage == "en") "Choose your language:" else "اختر لغتك المفضلة:",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LangCard(
                label = "العربية",
                emoji = "🇸🇦",
                selected = selectedLanguage == "ar",
                modifier = Modifier.weight(1f)
            ) { viewModel.selectLanguage("ar") }
            LangCard(
                label = "English",
                emoji = "🇬🇧",
                selected = selectedLanguage == "en",
                modifier = Modifier.weight(1f)
            ) { viewModel.selectLanguage("en") }
        }
    }
}

@Composable
private fun LangCard(
    label: String,
    emoji: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE0F2F1) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) Color(0xFF0E7075) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 28.sp)
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color(0xFF0E7075) else Color(0xFF334155)
            )
        }
    }
}

// ── Name & Gender Page ──────────────────────────────────────────────────────
@Composable
private fun NameGenderPage(
    nameInput: String,
    genderInput: String,
    showNameError: Boolean,
    selectedLanguage: String,
    onNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = {
                Text(if (selectedLanguage == "en") "Your Name" else "اسمك")
            },
            placeholder = {
                Text(
                    if (selectedLanguage == "en") "e.g. Ahmed" else "مثال: أحمد",
                    color = Color(0xFFB0B8C1)
                )
            },
            isError = showNameError && nameInput.trim().isEmpty(),
            supportingText = if (showNameError && nameInput.trim().isEmpty()) {
                { Text(if (selectedLanguage == "en") "Name is required" else "الاسم مطلوب", color = MaterialTheme.colorScheme.error) }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0E7075),
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedLabelColor = Color(0xFF0E7075),
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = if (selectedLanguage == "en") "I am:" else "أنا:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334155)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GenderCard(
                emoji = "👨",
                label = if (selectedLanguage == "en") "Male" else "ذكر",
                selected = genderInput == "male",
                color = Color(0xFF0E7075),
                bgColor = Color(0xFFE0F2F1),
                modifier = Modifier.weight(1f)
            ) { onGenderChange("male") }

            GenderCard(
                emoji = "👩",
                label = if (selectedLanguage == "en") "Female" else "أنثى",
                selected = genderInput == "female",
                color = Color(0xFFE91E63),
                bgColor = Color(0xFFFCE4EC),
                modifier = Modifier.weight(1f)
            ) { onGenderChange("female") }
        }
    }
}

@Composable
private fun GenderCard(
    emoji: String,
    label: String,
    selected: Boolean,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) bgColor else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) color else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 32.sp)
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) color else Color(0xFF334155)
            )
        }
    }
}

// ── Import Relatives Page ───────────────────────────────────────────────────
@Composable
private fun ImportRelativesPage(
    viewModel: RelativeViewModel,
    selectedLanguage: String,
    showRelativeError: Boolean
) {
    val currentRelatives by viewModel.relatives.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Import button
        Button(
            onClick = { viewModel.showImportContactsDialog.value = true },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0E7075),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (selectedLanguage == "en") "Import from Contacts 📱" else "استيراد من جهات الاتصال 📱",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (showRelativeError) {
            Text(
                if (selectedLanguage == "en") "⚠️ Please add at least one relative." else "⚠️ يرجى إضافة قريب واحد على الأقل.",
                fontSize = 12.sp,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (currentRelatives.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 38.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (selectedLanguage == "en") "No relatives added yet" else "لا يوجد أقارب مضافون بعد",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (selectedLanguage == "en") "Tap the button above to get started" else "اضغط الزر أعلاه للبدء",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B8C1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            Text(
                if (selectedLanguage == "en")
                    "Added (${currentRelatives.size}):"
                else
                    "المضافون (${currentRelatives.size}):",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentRelatives, key = { it.id }) { relative ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0E7075).copy(alpha = 0.25f)),
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                com.example.ui.components.RelativeAvatar(
                                    name = relative.name,
                                    photoUri = relative.photoUri,
                                    size = 38.dp,
                                    fontSize = 15.sp
                                )
                                Column {
                                    Text(
                                        relative.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    if (relative.phone.isNotBlank()) {
                                        Text(
                                            relative.phone,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteRelative(relative) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350),
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

// ── Summary Page ─────────────────────────────────────────────────────────────
@Composable
private fun SummaryPage(selectedLanguage: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val features = if (selectedLanguage == "en") listOf(
            Pair("🔔", "Smart reminders based on kinship closeness"),
            Pair("📞", "Auto-detects calls via your call log"),
            Pair("🔒", "100% private — data stays on your device"),
            Pair("✨", "Free forever, no ads, no subscriptions")
        ) else listOf(
            Pair("🔔", "تذكيرات ذكية بناءً على درجة القرابة"),
            Pair("📞", "كشف تلقائي للمكالمات عبر سجل الهاتف"),
            Pair("🔒", "خصوصية كاملة — بياناتك تبقى على جهازك"),
            Pair("✨", "مجاني للأبد، بدون إعلانات أو اشتراكات")
        )

        features.forEach { (emoji, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2F1))
                ) {
                    Text(emoji, fontSize = 18.sp)
                }
                Text(
                    text,
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

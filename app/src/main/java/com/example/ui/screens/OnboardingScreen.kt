package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
    val subtitleEn: String
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

    val onboardingItems = listOf(
        // Page 0: App Introduction & Language Selection
        OnboardingItem(
            titleAr = "مرحباً بك في صِلَةِ 🌿",
            titleEn = "Welcome to Silah 🌿",
            subtitleAr = "صِلَةِ هو رفيقك المقرب للعناية بأقاربك وصلة أرحامك بكل سهولة وخصوصية، رصد التفاعلات، والتذكير الدوري دون إعلانات أو تعقيد.",
            subtitleEn = "Silah is your personal companion to nurture family ties effortlessly and privately, with smart reminders and zero ads."
        ),
        // Page 1: Name & Gender Input
        OnboardingItem(
            titleAr = "ادخل اسمك",
            titleEn = "Enter Your Name",
            subtitleAr = "لتتمكن من تخصيص تجربتك وتوجيه التذكيرات باسمك",
            subtitleEn = "To personalize your app experience and tailored reminders"
        ),
        // Page 2: Initial Relatives Setup (NEW STEP)
        OnboardingItem(
            titleAr = "إضافة الأقارب والأرحام 👥",
            titleEn = "Add Initial Relatives 👥",
            subtitleAr = "أضف أفراد عائلتك المقربين للبدء في تتبع وتوثيق صلتهم فوراً",
            subtitleEn = "Add key family members to start tracking and documenting kin ties immediately"
        ),
        // Page 3: Auto Call Tracking & Reminders
        OnboardingItem(
            titleAr = "تذكيرات ومزامنة ذكية 🔔",
            titleEn = "Smart Sync & Reminders 🔔",
            subtitleAr = "رصد وتحديث مواعيد الاتصال بالأقارب وتنبيهات دورية تناسب درجة القربة",
            subtitleEn = "Automatic call log sync and periodic smart reminders tailored to kinship degrees"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Top Half: Teal Gradient Header ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0E7075),
                            Color(0xFF0D6367),
                            Color(0xFF094E51)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Circular Illustration Badge Container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(12.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.2f))
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (selectedLanguage == "en") onboardingItems[currentPage].titleEn else onboardingItems[currentPage].titleAr,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle / App Description
                Text(
                    text = if (selectedLanguage == "en") onboardingItems[currentPage].subtitleEn else onboardingItems[currentPage].subtitleAr,
                    fontSize = 13.sp,
                    color = Color(0xFFCBE5E7),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // ── Bottom Half: Content based on Page ─────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            when (currentPage) {
                // ── Page 0: Language Selector ─────────────────────────────────
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == "en") "Choose App Language:" else "اختر لغة التطبيق المفضلة:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.selectLanguage("ar") }
                            ) {
                                Text("العربية", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.width(6.dp))
                                RadioButton(
                                    selected = selectedLanguage == "ar",
                                    onClick = { viewModel.selectLanguage("ar") },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0E7075))
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.selectLanguage("en") }
                            ) {
                                Text("English", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.width(6.dp))
                                RadioButton(
                                    selected = selectedLanguage == "en",
                                    onClick = { viewModel.selectLanguage("en") },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0E7075))
                                )
                            }
                        }
                    }
                }

                // ── Page 1: Name & Gender Entry ───────────────────────────────
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Outlined Name Field
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                if (it.trim().isNotEmpty()) showNameError = false
                            },
                            placeholder = {
                                Text(
                                    if (selectedLanguage == "en") "Enter your name..." else "أدخل اسمك هنا...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            },
                            isError = showNameError && nameInput.trim().isEmpty(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0E7075),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                errorBorderColor = Color(0xFFD32F2F),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (showNameError && nameInput.trim().isEmpty()) {
                            Text(
                                text = if (selectedLanguage == "en") "Please enter your name first!" else "يرجى كتابة الاسم أولاً للبدء!",
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Gender Selection Cards
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Male Card
                            Card(
                                onClick = { genderInput = "male" },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (genderInput == "male") Color(0xFFE0F2F1) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (genderInput == "male") Color(0xFF0E7075) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    com.example.ui.components.SilaUserAvatar(
                                        avatarId = "avatar_01",
                                        size = 46.dp,
                                        showBorder = genderInput == "male"
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (selectedLanguage == "en") "Male" else "ذكر 👦",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            // Female Card
                            Card(
                                onClick = { genderInput = "female" },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (genderInput == "female") Color(0xFFFCE4EC) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (genderInput == "female") Color(0xFFE91E63) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    com.example.ui.components.SilaUserAvatar(
                                        avatarId = "avatar_02",
                                        size = 46.dp,
                                        showBorder = genderInput == "female"
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (selectedLanguage == "en") "Female" else "أنثى 👧",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Page 2: Add Initial Relatives Step (NEW STEP) ───────────────
                2 -> {
                    var quickName by remember { mutableStateOf("") }
                    var quickRelation by remember { mutableStateOf("أم") }
                    val currentRelatives by viewModel.relatives.collectAsState()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedLanguage == "en") "Quickly add key relatives:" else "أضف أقاربك المفضلين بنقرة واحدة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        // BUG-06 Fix: use explicit name field to avoid fragile substringBefore(" ") on labels
                        val presetKin = listOf(
                            Triple("الأم 🌸",  "الأم",  "أم"),
                            Triple("الأب 👨\u200d👧", "الأب",  "أب"),
                            Triple("الأخ 👦",  "الأخ",  "أخ"),
                            Triple("الأخت 👧", "الأخت", "أخت"),
                            Triple("العم 🤝",  "العم",  "عم"),
                            Triple("الخال 💚", "الخال", "خال")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            presetKin.take(3).forEach { (label, name, rel) ->
                                FilterChip(
                                    selected = currentRelatives.any { it.relationshipDegree == rel },
                                    onClick = {
                                        // BUG-05 Fix: allow unselecting (deselect = delete)
                                        val existing = currentRelatives.firstOrNull { it.relationshipDegree == rel }
                                        if (existing != null) {
                                            viewModel.deleteRelative(existing)
                                        } else {
                                            viewModel.addRelative(
                                                name = name,
                                                phone = "",
                                                relationshipDegree = rel,
                                                intervalDays = 7,
                                                notes = ""
                                            )
                                        }
                                    },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            presetKin.drop(3).forEach { (label, name, rel) ->
                                FilterChip(
                                    selected = currentRelatives.any { it.relationshipDegree == rel },
                                    onClick = {
                                        // BUG-05 Fix: allow unselecting (deselect = delete)
                                        val existing = currentRelatives.firstOrNull { it.relationshipDegree == rel }
                                        if (existing != null) {
                                            viewModel.deleteRelative(existing)
                                        } else {
                                            viewModel.addRelative(
                                                name = name,
                                                phone = "",
                                                relationshipDegree = rel,
                                                intervalDays = 7,
                                                notes = ""
                                            )
                                        }
                                    },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }

                        // Contact Import Option Button
                        OutlinedButton(
                            onClick = { viewModel.showImportContactsDialog.value = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0E7075)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedLanguage == "en") "Import from Contacts 📱" else "استيراد من جهات الاتصال 📱",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0E7075)
                            )
                        }

                        if (currentRelatives.isNotEmpty()) {
                            Text(
                                text = if (selectedLanguage == "en") "Added ${currentRelatives.size} relatives ✅" else "تمت إضافة ${currentRelatives.size} من الأقارب ✅",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        } else if (showRelativeError) {
                            Text(
                                text = if (selectedLanguage == "en") "Please add at least one relative to continue! ⚠️" else "يرجى إضافة قريب واحد على الأقل للاستمرار! ⚠️",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                // ── Page 3: Summary & Ready ────────────────────────────────────
                else -> {
                    Text(
                        text = if (selectedLanguage == "en") {
                            "You are all set! Silah will keep your kin relationships organized, warm, and blooming every day."
                        } else {
                            "أنت جاهز الآن! صِلَةِ سينظم تواصلك ويوثق صلة أرحامك لتنال البركة والرضوان يومياً."
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // ── Navigation Action Row (With proper RTL Arrow Directions) ────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Show Back Button if on Page > 0
                if (currentPage > 0) {
                    Surface(
                        onClick = { currentPage-- },
                        shape = CircleShape,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Back Arrow: In RTL (Arabic), back points RIGHT ➡️
                            Icon(
                                imageVector = if (selectedLanguage == "en") Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "السابق",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                // Wide Next Pill Button with Forward Arrow (Left ⬅️ in Arabic RTL)
                Button(
                    onClick = {
                        if (currentPage == 1) {
                            if (nameInput.trim().isEmpty()) {
                                showNameError = true
                                android.widget.Toast.makeText(
                                    context,
                                    if (selectedLanguage == "en") "Please enter your name first!" else "الرجاء كتابة اسمك أولاً للاستمرار!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            showNameError = false
                            viewModel.saveUserProfile(nameInput.trim(), genderInput)
                        }
                        if (currentPage == 2) {
                            val relativesList = viewModel.relatives.value
                            if (relativesList.isEmpty()) {
                                showRelativeError = true
                                android.widget.Toast.makeText(
                                    context,
                                    if (selectedLanguage == "en") "Please add at least one relative to continue!" else "يرجى إضافة قريب واحد على الأقل للاستمرار!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            showRelativeError = false
                        }
                        if (currentPage < onboardingItems.size - 1) {
                            currentPage++
                        } else {
                            viewModel.showImportContactsDialog.value = false
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0E7075),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentPage < onboardingItems.size - 1) {
                                if (selectedLanguage == "en") "Next" else "التالي"
                            } else {
                                if (selectedLanguage == "en") "Start Now 🌸" else "ابدأ الآن 🌸"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentPage < onboardingItems.size - 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Next Arrow: In RTL (Arabic), forward/next points LEFT ⬅️
                            Icon(
                                imageVector = if (selectedLanguage == "en") Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "التالي",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ── 4-Dot Page Indicator ───────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                onboardingItems.indices.forEach { index ->
                    val isActive = index == currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color(0xFFD97706) else Color(0xFFCBD5E1)
                            )
                    )
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

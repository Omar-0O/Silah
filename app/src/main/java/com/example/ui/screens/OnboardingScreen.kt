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

    var nameInput by remember { mutableStateOf(savedName) }
    var genderInput by remember { mutableStateOf(savedGender) }
    var currentPage by remember { mutableIntStateOf(0) }
    var showNameError by remember { mutableStateOf(false) }

    val onboardingItems = listOf(
        // Page 0: Language Selection
        OnboardingItem(
            titleAr = "تطبيق صِلَةِ",
            titleEn = "Silah App",
            subtitleAr = "رفيقك الذكي لمتابعة تواصلك اليومي\nمع أرحامك وأقاربك بسهولة ويسر",
            subtitleEn = "Your smart companion to track your daily connection with your relatives effortlessly"
        ),
        // Page 1: Name & Gender Input (Matching User Screenshot)
        OnboardingItem(
            titleAr = "ادخل اسمك",
            titleEn = "Enter Your Name",
            subtitleAr = "لتتمكن من تخصيص تجربتك",
            subtitleEn = "To personalize your app experience"
        ),
        // Page 2: Auto Call Tracking
        OnboardingItem(
            titleAr = "مزامنة المكالمات تلقائياً",
            titleEn = "Auto Call Tracking",
            subtitleAr = "رصد وتحديث مواعيد الاتصال بالأقارب تلقائياً\nدون الحاجة للتسجيل اليدوي",
            subtitleEn = "Automatically logs call times with relatives without manual input"
        ),
        // Page 3: Smart Reminders
        OnboardingItem(
            titleAr = "تذكيرات وإشعارات ذكية",
            titleEn = "Smart Reminders",
            subtitleAr = "تنبيهات دورية تناسب درجة القربة\nلتبقى دائماً على تواصل دائم",
            subtitleEn = "Periodic smart notifications tailored to kinship degrees"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Top Half: Teal Gradient Header matching user screenshot ──────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
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
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Circular Illustration Badge Container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(12.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.2f))
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = if (selectedLanguage == "en") onboardingItems[currentPage].titleEn else onboardingItems[currentPage].titleAr,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = if (selectedLanguage == "en") onboardingItems[currentPage].subtitleEn else onboardingItems[currentPage].subtitleAr,
                    fontSize = 14.sp,
                    color = Color(0xFFCBE5E7),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
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
                .weight(1f)
                .background(Color.White)
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            when (currentPage) {
                // ── Page 0: Language Selector ─────────────────────────────────
                0 -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.selectLanguage("ar") }
                        ) {
                            Text("العربية", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = selectedLanguage == "ar",
                                onClick = { viewModel.selectLanguage("ar") },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD97706), unselectedColor = Color(0xFF94A3B8))
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.selectLanguage("en") }
                        ) {
                            Text("English", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = selectedLanguage == "en",
                                onClick = { viewModel.selectLanguage("en") },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD97706), unselectedColor = Color(0xFF94A3B8))
                            )
                        }
                    }
                }

                // ── Page 1: Name & Gender Entry (User Screenshot Match) ───────
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                    fontSize = 15.sp
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
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // Gender Selection Cards with Character Avatar Preview
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Male Card (ولد)
                            Card(
                                onClick = { genderInput = "male" },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (genderInput == "male") Color(0xFFE0F2F1) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (genderInput == "male") Color(0xFF0E7075) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    com.example.ui.components.SilaUserAvatar(
                                        avatarId = "avatar_01",
                                        size = 50.dp,
                                        showBorder = genderInput == "male"
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = genderInput == "male",
                                            onClick = { genderInput = "male" },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0E7075))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (selectedLanguage == "en") "Boy" else "ولد (ذكر)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }

                            // Female Card (بنت)
                            Card(
                                onClick = { genderInput = "female" },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (genderInput == "female") Color(0xFFFCE4EC) else Color(0xFFF8FAFC)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (genderInput == "female") Color(0xFFE91E63) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    com.example.ui.components.SilaUserAvatar(
                                        avatarId = "avatar_02",
                                        size = 50.dp,
                                        showBorder = genderInput == "female"
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = genderInput == "female",
                                            onClick = { genderInput = "female" },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE91E63))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (selectedLanguage == "en") "Girl" else "بنت (أنثى)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Pages 2 & 3: Feature Info Cards ───────────────────────────
                else -> {
                    Text(
                        text = if (selectedLanguage == "en") {
                            "Welcome to Silah! We're happy to help you stay closely connected with your family."
                        } else {
                            "أهلاً بك في صِلَةِ! يسعدنا مساعدتك لتبقى دائماً أقرب لأقاربك وأرحامك."
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // ── Navigation Action Row ──────────────────────────────────────────
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
                            Icon(
                                imageVector = if (selectedLanguage == "en") Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "السابق",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Wide Next Pill Button
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
                            // Save profile name & gender
                            viewModel.saveUserProfile(nameInput.trim(), genderInput)
                        }
                        if (currentPage < onboardingItems.size - 1) {
                            currentPage++
                        } else {
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
                    Text(
                        text = if (currentPage < onboardingItems.size - 1) {
                            if (selectedLanguage == "en") "Next" else "التالي"
                        } else {
                            if (selectedLanguage == "en") "Start Now" else "ابدأ الآن 🌸"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── 4-Dot Page Indicator matching user screenshot ──────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
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
}

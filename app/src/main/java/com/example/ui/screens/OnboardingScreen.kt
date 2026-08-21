package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KinshipKnotIcon
import com.example.ui.theme.SoftGold

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val highlightText: String
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            emoji = "🌿",
            title = "صِلَةِ",
            subtitle = "رَبِّ أرحامَكَ بخطوة واحدة يسيرة",
            highlightText = "«مَنْ سَرَّهُ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ فَلْيَصِلْ رَحِمَهُ»"
        ),
        OnboardingPage(
            emoji = "📞",
            title = "مزامنة تلقائية",
            subtitle = "يرصد التطبيق مكالماتك تلقائياً ويعرفك لو تواصلت مع قريب أو اتصل بك",
            highlightText = "لا تحتاج لتسجيل كل مكالمة يدوياً — صِلَةِ يتابع بدلاً عنك"
        ),
        OnboardingPage(
            emoji = "🔔",
            title = "تذكيرات دورية",
            subtitle = "ضع كل قريب في قائمتك واضبط موعد تذكيرك بالاطمئنان عليه",
            highlightText = "نسخة احتياطية دائمة — بياناتك محفوظة على هاتفك وتقدر ترفعها على Drive"
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F3620), Color(0xFF1E5A35), Color(0xFF143B23))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Page Content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                },
                label = "page_transition",
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Emoji icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0x18FFFFFF))
                    ) {
                        if (page == 0) {
                            KinshipKnotIcon(color = SoftGold, modifier = Modifier.size(56.dp))
                        } else {
                            Text(pages[page].emoji, fontSize = 52.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = pages[page].title,
                        fontSize = if (page == 0) 38.sp else 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = pages[page].subtitle,
                        fontSize = 15.sp,
                        color = Color(0xFFBCCEC3),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = pages[page].highlightText,
                            fontSize = 13.sp,
                            color = SoftGold,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (index == currentPage) SoftGold else Color.White.copy(alpha = 0.3f))
                            .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                    )
                }
            }

            // Navigation Buttons
            if (currentPage < pages.size - 1) {
                Button(
                    onClick = { currentPage++ },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("التالي ←", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onFinished) {
                    Text("تخطّ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("ابدأ مسيرة صِلَةِ الآن 🌸", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

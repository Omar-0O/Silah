package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KinshipKnotIcon
import com.example.ui.theme.SoftGold
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val verses = listOf(
        "«وَاتَّقُوا اللَّهَ الَّذِي تَسَاءَلُونَ بِهِ وَالْأَرْحَامَ ۚ إِنَّ اللَّهَ كَانَ عَلَيْكُمْ رَقِيبًا»\n[النساء: 1]",
        "«وَالَّذِينَ يَصِلُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيَخْشَوْنَ رَبَّهُمْ»\n[الرعد: 21]",
        "«مَنْ سَرَّهُ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ، وَأَنْ يُنْسَأَ لَهُ فِي أَثَرِهِ، فَلْيَصِلْ رَحِمَهُ»\n[حديث شريف]"
    )
    var currentVerseIndex by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
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
            // Gold floral pattern emblem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0x15E9CE79))
                    .border(1.5.dp, SoftGold.copy(alpha = 0.5f), CircleShape)
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
                    KinshipKnotIcon(
                        color = SoftGold,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "صِلَةِ",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
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
                            color = SoftGold,
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

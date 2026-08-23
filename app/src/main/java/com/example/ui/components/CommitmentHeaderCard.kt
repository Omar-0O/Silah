package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftGold
import java.text.SimpleDateFormat
import java.util.*

private data class QuranVerse(
    val verseText: String,
    val surahInfo: String
)

/**
 * Commitment Header Card (Stitch-style)
 * Features:
 * - Deep Olive gradient background (Deep Forest Green → Dark Emerald)
 * - Animated Circular Progress Arc (SoftGold)
 * - Personalized greeting
 * - Kin-tie stats
 */
@Composable
fun CommitmentHeaderCard(
    totalLogsCount: Int,
    uniqueDaysCount: Int,
    uniqueRelativesContacted: Int,
    lang: String = "ar",
    userName: String = "",
    totalRelativesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    // Commitment score: % of relatives contacted at least once
    val commitmentPct = if (totalRelativesCount == 0) 0f
                        else (uniqueRelativesContacted.toFloat() / totalRelativesCount).coerceIn(0f, 1f)

    // Animate the arc
    val animatedArc by animateFloatAsState(
        targetValue = commitmentPct,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "commitment_arc"
    )

    // Arabic date
    val dateLocale = if (lang == "en") Locale.ENGLISH else Locale("ar", "SA")
    val arabicDate = remember {
        SimpleDateFormat("EEEE، d MMMM yyyy", dateLocale).format(Date())
    }

    // Time-based greeting
    val greeting = remember(lang, userName) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = if (lang == "en") when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else      -> "Good evening"
        } else when {
            hour < 12 -> "صباح الخير"
            hour < 17 -> "مساء الخير"
            else      -> "مساء النور"
        }
        val name = if (userName.isNotBlank()) userName else if (lang == "en") "" else ""
        if (name.isNotBlank()) "$timeGreeting، $name 🌿" else "$timeGreeting 🌿"
    }

    // Daily Quran Verse Rotator
    val todayVerse = remember {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val quranVerses = listOf(
            QuranVerse(
                verseText = "﴿وَاتَّقُوا اللَّهَ الَّذِي تَسَاءَلُونَ بِهِ وَالْأَرْحَامَ ۚ إِنَّ اللَّهَ كَانَ عَلَيْكُمْ رَقِيبًا﴾",
                surahInfo = "سورة النساء – آية ١"
            ),
            QuranVerse(
                verseText = "﴿وَالَّذِينَ يَصِلُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيَخْشَوْنَ رَبَّهُمْ وَيَخَافُونَ سُوءَ الْحِسَابِ﴾",
                surahInfo = "سورة الرعد – آية ٢١"
            ),
            QuranVerse(
                verseText = "﴿الَّذِينَ يَنقُضُونَ عَهْدَ اللَّهِ مِن بَعْدِ مِيثَاقِهِ وَيَقْطَعُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيُفْسِدُونَ فِي الْأَرْضِ ۚ أُولَٰئِكَ هُمُ الْخَاسِرُونَ﴾",
                surahInfo = "سورة البقرة – آية ٢٧"
            ),
            QuranVerse(
                verseText = "﴿فَهَلْ عَسَيْتُمْ إِن تَوَلَّيْتُمْ أَن تُفْسِدُوا فِي الْأَرْضِ وَتُقَطِّعُوا أَرْحَامَكُمْ ۝ أُولَٰئِكَ الَّذِينَ لَعَنَهُمُ اللَّهُ فَأَصَمَّهُمْ وَأَعْمَىٰ أَبْصَارَهُمْ﴾",
                surahInfo = "سورة محمد – آية ٢٢-٢٣"
            ),
            QuranVerse(
                verseText = "﴿وَالَّذِينَ يَنقُضُونَ عَهْدَ اللَّهِ مِن بَعْدِ مِيثَاقِهِ وَيَقْطَعُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيُفْسِدُونَ فِي الْأَرْضِ ۙ أُولَٰئِكَ لَهُمُ اللَّعْنَةُ وَلَهُمْ سُوءُ الدَّارِ﴾",
                surahInfo = "سورة الرعد – آية ٢٥"
            )
        )
        quranVerses[(dayOfYear - 1) % quranVerses.size]
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                16.dp,
                RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF1E5A35).copy(alpha = 0.35f),
                spotColor = Color(0xFF1E5A35).copy(alpha = 0.25f)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF16503A), Color(0xFF0D3324), Color(0xFF082416))
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Top Row: Date + Arc ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Greeting + date + Daily Verse
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = arabicDate,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Daily Verse Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = todayVerse.verseText,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = todayVerse.surahInfo,
                                fontSize = 10.sp,
                                color = SoftGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Circular commitment arc
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(88.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 7.dp.toPx()
                        val inset = strokeW / 2
                        val arcSize = Size(size.width - strokeW, size.height - strokeW)
                        val arcOffset = Offset(inset, inset)

                        // Track (background arc)
                        drawArc(
                            color = Color.White.copy(alpha = 0.12f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(strokeW, cap = StrokeCap.Round)
                        )

                        // Progress arc (SoftGold)
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFE9CE79),
                                    Color(0xFFF5D278),
                                    Color(0xFFD4A843)
                                )
                            ),
                            startAngle = 135f,
                            sweepAngle = animatedArc * 270f,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(strokeW, cap = StrokeCap.Round)
                        )
                    }

                    // Percentage label inside arc
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(commitmentPct * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SoftGold
                        )
                        Text(
                            text = if (lang == "en") "Tied" else "صلة",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // ── Stat Chips Row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    emoji = "🤝",
                    value = totalLogsCount.toString(),
                    label = if (lang == "en") "Total Logs" else "إجمالي الصلات",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    emoji = "📅",
                    value = uniqueDaysCount.toString(),
                    label = if (lang == "en") "Days Active" else "أيام الصلة",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    emoji = "👥",
                    value = uniqueRelativesContacted.toString(),
                    label = if (lang == "en") "Relatives" else "أقارب",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0x20FFFFFF), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFFD0E0D5),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

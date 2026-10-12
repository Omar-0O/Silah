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

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import com.example.ui.theme.SilaShapes
import com.example.ui.theme.spacing

/**
 * Commitment Header Card
 * Features:
 * - Deep Olive gradient background (Deep Forest Green → Dark Emerald)
 * - Spring-animated Circular Progress Arc (SoftGold)
 * - Personalized greeting & rotating Quran Ayahs
 * - Glassmorphic Kin-tie stat chips
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
    val spacing = MaterialTheme.spacing

    // Commitment score: % of relatives contacted at least once
    val commitmentPct = if (totalRelativesCount == 0) 0f
                        else (uniqueRelativesContacted.toFloat() / totalRelativesCount).coerceIn(0f, 1f)

    // Animate the arc with spring physics for natural feel
    val animatedArc by animateFloatAsState(
        targetValue = commitmentPct,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "commitment_arc"
    )

    // Arabic date
    val dateLocale = if (lang == "en") Locale.ENGLISH else Locale.forLanguageTag("ar-SA")
    val arabicDate = remember {
        SimpleDateFormat("EEEE، d MMMM yyyy", dateLocale).format(Date())
    }

    // Time-based greeting
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeGreeting = when {
        hour < 12 -> androidx.compose.ui.res.stringResource(com.example.R.string.greeting_morning)
        hour < 17 -> androidx.compose.ui.res.stringResource(com.example.R.string.greeting_afternoon)
        else      -> androidx.compose.ui.res.stringResource(com.example.R.string.greeting_evening)
    }
    val greeting = if (userName.isNotBlank()) "$timeGreeting، $userName 🌿" else "$timeGreeting 🌿"

    // Quran Ayahs on Kinship Ties (rotates on each app session)
    val quranAyahs = remember {
        listOf(
            "﴿وَاتَّقُوا اللَّهَ الَّذِي تَسَاءَلُونَ بِهِ وَالْأَرْحَامَ ۚ إِنَّ اللَّهَ كَانَ عَلَيْكُمْ رَقِيبًا﴾" to "سورة النساء – آية 1",
            "﴿وَالَّذِينَ يَصِلُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيَخْشَوْنَ رَبَّهُمْ وَيَخَافُونَ سُوءَ الْحِسَابِ﴾" to "سورة الرعد – آية 21",
            "﴿الَّذِينَ يَنقُضُونَ عَهْدَ اللَّهِ مِن بَعْدِ مِيثَاقِهِ وَيَقْطَعُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيُفْسِدُونَ فِي الْأَرْضِ ۚ أُولَٰئِكَ هُمُ الْخَاسِرُونَ﴾" to "سورة البقرة – آية 27",
            "﴿فَهَلْ عَسَيْتُمْ إِن تَوَلَّيْتُمْ أَن تُفْسِدُوا فِي الْأَرْضِ وَتُقَطِّعُوا أَرْحَامَكُمْ ۝ أُولَٰئِكَ الَّذِينَ لَعَنَهُمُ اللَّهُ فَأَصَمَّهُمْ وَأَعْمَىٰ أَبْصَارَهُمْ﴾" to "سورة محمد – آية 22-23",
            "﴿وَالَّذِينَ يَنقُضُونَ عَهْدَ اللَّهِ مِن بَعْدِ مِيثَاقِهِ وَيَقْطَعُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيُفْسِدُونَ فِي الْأَرْضِ ۙ أُولَٰئِكَ لَهُمُ اللَّعْنَةُ وَلَهُمْ سُوءُ الدَّارِ﴾" to "سورة الرعد – آية 25"
        )
    }

    val selectedAyah = remember { quranAyahs.random() }

    Card(
        shape = SilaShapes.CardLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = SilaShapes.CardLarge,
                ambientColor = Color(0xFF16503A).copy(alpha = 0.25f),
                spotColor = Color(0xFF0D3324).copy(alpha = 0.35f)
            )
            .clip(SilaShapes.CardLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF16503A), Color(0xFF0D3324), Color(0xFF082416))
                )
            )
    ) {
        Column(modifier = Modifier.padding(spacing.xl)) {

            // ── Top Row: Date + Arc ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Greeting + date + Quran Ayah
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
                    Text(
                        text = selectedAyah.first,
                        fontSize = 12.5.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedAyah.second,
                        fontSize = 10.5.sp,
                        color = SoftGold.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Normal
                    )
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
                            text = androidx.compose.ui.res.stringResource(com.example.R.string.tied_label),
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
                    value = totalLogsCount.toString(),
                    label = androidx.compose.ui.res.stringResource(com.example.R.string.total_logs_stat),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    value = uniqueDaysCount.toString(),
                    label = androidx.compose.ui.res.stringResource(com.example.R.string.days_active_stat),
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    value = uniqueRelativesContacted.toString(),
                    label = androidx.compose.ui.res.stringResource(com.example.R.string.relatives_stat),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = SilaShapes.ActionCard,
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFFDCECE1),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

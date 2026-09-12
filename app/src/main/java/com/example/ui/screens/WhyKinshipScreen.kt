package com.example.ui.screens

import android.content.Intent
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.IslamicFaqItem
import com.example.ui.dialogs.islamicFaqList
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SoftGold

enum class FaqCategory(val titleAr: String, val titleEn: String) {
    ALL("الكل", "All"),
    QURAN("القرآن الكريم", "Holy Quran"),
    SUNNAH("السنّة النبوية", "Prophetic Sunnah")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhyKinshipScreen(
    lang: String = "ar",
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current

    var selectedCategory by remember { mutableStateOf(FaqCategory.ALL) }
    var expandedQuestion by remember { mutableStateOf<String?>(null) }

    // Filter items based on category
    val filteredList = remember(selectedCategory) {
        when (selectedCategory) {
            FaqCategory.ALL -> islamicFaqList
            FaqCategory.QURAN -> islamicFaqList.take(10)
            FaqCategory.SUNNAH -> islamicFaqList.drop(10)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (lang == "en") "Why Maintain Kin Ties?" else "ليه أصل رحمي؟",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (lang == "en") "Questions & answers from Quran and Sunnah" else "إجابات وبصائر من القرآن والسنّة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (lang == "en") "Back" else "رجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Hero Motivation Banner ──────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color(0xFF16503A).copy(alpha = 0.3f),
                            spotColor = Color(0xFF16503A).copy(alpha = 0.2f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF16503A), Color(0xFF0D3324), Color(0xFF082416))
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SoftGold.copy(alpha = 0.2f))
                            ) {
                                Text("✨", fontSize = 16.sp)
                            }
                            Text(
                                text = if (lang == "en") "The Nobility of Kinship Ties" else "عظمة ومكانة صلة الرحم",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftGold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "«مَنْ أَحَبَّ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ وَيُنْسَأَ لَهُ فِي أَثَرِهِ فَلْيَصِلْ رَحِمَهُ»",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (lang == "en")
                                "Kinship ties are not merely a social courtesy, but a profound devotion bringing Allah's mercy, expansive sustenance, and barakah."
                            else
                                "صلة الرحم ليست مجرد واجب اجتماعي، بل هي عبادة وقربة جليلة تزيد الرزق وتبارك في العمر وتجلب معية الله ورضوانه.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── 2. Category Filter Chips ──────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                        items(FaqCategory.entries) { cat ->
                            val isSelected = selectedCategory == cat
                            val count = when (cat) {
                                FaqCategory.ALL -> islamicFaqList.size
                                FaqCategory.QURAN -> 10
                                FaqCategory.SUNNAH -> islamicFaqList.size - 10
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    selectedCategory = cat
                                },
                                label = {
                                    Text(
                                        text = "${if (lang == "en") cat.titleEn else cat.titleAr} ($count)",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

            // ── 3. Questions Count Info ────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "en") "${filteredList.size} Questions Available" else "${filteredList.size} سؤالاً وجواباً",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (lang == "en") "Tap question to expand" else "اضغط على السؤال لعرض الإجابة والآية",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── 4. Questions List ──────────────────────────────────────────
            itemsIndexed(filteredList, key = { index, item -> item.question }) { index, item ->
                val isExpanded = expandedQuestion == item.question

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isExpanded) PrimaryGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Question row (clickable header)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    expandedQuestion = if (isExpanded) null else item.question
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Question Number Badge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isExpanded) PrimaryGreen else PrimaryGreen.copy(alpha = 0.12f)
                                        )
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpanded) Color.White else PrimaryGreen
                                    )
                                }

                                Text(
                                    text = item.question,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Answer section (expanded)
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                            exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Answer Card / Box
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.answer,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 24.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                // Reference Tag & Action Buttons (Share / Copy)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Reference chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryGreen.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = item.reference,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreen
                                        )
                                    }

                                    // Action icons (Copy + Share)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Copy
                                        IconButton(
                                            onClick = {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                val copyText = "${item.question}\n\n${item.answer}\n\n${item.reference}"
                                                clipboardManager.setText(AnnotatedString(copyText))
                                                Toast.makeText(
                                                    context,
                                                    if (lang == "en") "Copied to clipboard ✨" else "تم نسخ الإجابة والدليل ✨",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ContentCopy,
                                                contentDescription = if (lang == "en") "Copy" else "نسخ",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }

                                        // Share via WhatsApp / messaging
                                        IconButton(
                                            onClick = {
                                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                val shareText = "❓ ${item.question}\n\n📖 ${item.answer}\n\n📌 المصدر: ${item.reference}\n\n— عبر تطبيق صِلَةِ 🌿"
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(
                                                    sendIntent,
                                                    if (lang == "en") "Share via" else "مشاركة عبر"
                                                )
                                                context.startActivity(shareIntent)
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Share,
                                                contentDescription = if (lang == "en") "Share" else "مشاركة",
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacer for comfortable scrolling
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

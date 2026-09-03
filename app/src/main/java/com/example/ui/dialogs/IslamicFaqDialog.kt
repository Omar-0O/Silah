package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class IslamicFaqItem(
    val question: String,
    val answer: String,
    val reference: String
)

val islamicFaqList = listOf(
    IslamicFaqItem(
        question = "لماذا يجب أن أحافظ على صلة رحمي أصلًا؟",
        answer = "﴿يَا أَيُّهَا النَّاسُ اتَّقُوا رَبَّكُمُ الَّذِي خَلَقَكُم مِّن نَّفْسٍ وَاحِدَةٍ وَخَلَقَ مِنْهَا زَوْجَهَا وَبَثَّ مِنْهُمَا رِجَالًا كَثِيرًا وَنِسَاءً ۚ وَاتَّقُوا اللَّهَ الَّذِي تَسَاءَلُونَ بِهِ وَالْأَرْحَامَ ۚ إِنَّ اللَّهَ كَانَ عَلَيْكُمْ رَقِيبًا﴾",
        reference = "[النساء: 1]"
    ),
    IslamicFaqItem(
        question = "هل صلة الرحم من الأشياء التي أمر الله أن تُوصل؟",
        answer = "﴿وَالَّذِينَ يَصِلُونَ مَا أَمَرَ اللَّهُ بِهِ أَنْ يُوصَلَ وَيَخْشَوْنَ رَبَّهُمْ وَيَخَافُونَ سُوءَ الْحِسَابِ﴾\n\nوقد ذكر المفسرون أن صلة الأرحام من أبرز ما يدخل في الآية.",
        reference = "[الرعد: 21]"
    ),
    IslamicFaqItem(
        question = "ماذا يحدث لمن يقطع ما أمر الله بوصله؟",
        answer = "﴿الَّذِينَ يَنقُضُونَ عَهْدَ اللَّهِ مِن بَعْدِ مِيثَاقِهِ وَيَقْطَعُونَ مَا أَمَرَ اللَّهُ بِهِ أَن يُوصَلَ وَيُفْسِدُونَ فِي الْأَرْضِ ۚ أُولَٰئِكَ هُمُ الْخَاسِرُونَ﴾",
        reference = "[البقرة: 27]"
    ),
    IslamicFaqItem(
        question = "هل قطع الرحم ذنب خطير؟",
        answer = "﴿وَالَّذِينَ يَنقُضُونَ عَهْدَ اللَّهِ مِن بَعْدِ مِيثَاقِهِ وَيَقْطَعُونَ مَا أَمَرَ اللَّهُ بِهِ أَنْ يُوصَلَ وَيُفْسِدُونَ فِي الْأَرْضِ أُولَٰئِكَ لَهُمُ اللَّعْنَةُ وَلَهُمْ سُوءُ الدَّارِ﴾",
        reference = "[الرعد: 25]"
    ),
    IslamicFaqItem(
        question = "هل القرآن ذكر قطع الأرحام بالاسم؟",
        answer = "﴿فَهَلْ عَسَيْتُمْ إِن تَوَلَّيْتُمْ أَن تُفْسِدُوا فِي الْأَرْضِ وَتُقَطِّعُوا أَرْحَامَكُمْ﴾",
        reference = "[محمد: 22]"
    ),
    IslamicFaqItem(
        question = "هل الله أمرني بالإحسان إلى أقاربي؟",
        answer = "﴿وَاعْبُدُوا اللَّهَ وَلَا تُشْرِكُوا بِهِ شَيْئًا وَبِالْوَالِدَيْنِ إِحْسَانًا وَبِذِي الْقُرْبَىٰ...﴾",
        reference = "[النساء: 36]"
    ),
    IslamicFaqItem(
        question = "هل لأقاربي حق عليّ؟",
        answer = "﴿وَآتِ ذَا الْقُرْبَىٰ حَقَّهُ وَالْمِسْكِينَ وَابْنَ السَّبِيلِ﴾",
        reference = "[الإسراء: 26]"
    ),
    IslamicFaqItem(
        question = "هل إعطاء الأقارب والإحسان إليهم من الأعمال التي يحبها الله؟",
        answer = "﴿فَآتِ ذَا الْقُرْبَىٰ حَقَّهُ وَالْمِسْكِينَ وَابْنَ السَّبِيلِ ۚ ذَٰلِكَ خَيْرٌ لِلَّذِينَ يُرِيدُونَ وَجْهَ اللَّهِ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ﴾",
        reference = "[الروم: 38]"
    ),
    IslamicFaqItem(
        question = "ماذا لو غضبت من قريبي ولا أريد أن أعطيه أو أساعده؟",
        answer = "﴿وَلَا يَأْتَلِ أُولُو الْفَضْلِ مِنكُمْ وَالسَّعَةِ أَن يُؤْتُوا أُولِي الْقُرْبَىٰ وَالْمَسَاكِينَ وَالْمُهَاجِرِينَ فِي سَبِيلِ اللَّهِ ۖ وَلْيَعْفُوا وَلْيَصْفَحُوا ۗ أَلَا تُحِبُّونَ أَن يَغْفِرَ اللَّهُ لَكُمْ ۗ وَاللَّهُ غَفُورٌ رَّحِيمٌ﴾",
        reference = "[النور: 22]"
    ),
    IslamicFaqItem(
        question = "هل للأقارب أولوية خاصة؟",
        answer = "﴿وَأُولُو الْأَرْحَامِ بَعْضُهُمْ أَوْلَىٰ بِبَعْضٍ فِي كِتَابِ اللَّهِ﴾",
        reference = "[الأنفال: 75]"
    ),
    IslamicFaqItem(
        question = "هل صلة الرحم مرتبطة بالإيمان؟",
        answer = "قال رسول الله ﷺ:\n«مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَصِلْ رَحِمَهُ».",
        reference = "متفق عليه – صحيح البخاري 6138"
    ),
    IslamicFaqItem(
        question = "هل صلة الرحم ممكن تكون سببًا في زيادة الرزق؟",
        answer = "قال رسول الله ﷺ:\n«مَنْ أَحَبَّ أَنْ يُبْسَطَ لَهُ فِي رِزْقِهِ وَيُنْسَأَ لَهُ فِي أَثَرِهِ فَلْيَصِلْ رَحِمَهُ».",
        reference = "رواه مسلم 2557"
    ),
    IslamicFaqItem(
        question = "ماذا يفعل الله بمن يحافظ على صلة رحمه؟",
        answer = "قال النبي ﷺ:\n«مَنْ وَصَلَهَا وَصَلَهُ اللَّهُ، وَمَنْ قَطَعَهَا قَطَعَهُ اللَّهُ».",
        reference = "رواه البخاري، وجاء في الأدب المفرد"
    ),
    IslamicFaqItem(
        question = "ما خطورة قطيعة الرحم؟",
        answer = "قال رسول الله ﷺ:\n«لَا يَدْخُلُ الْجَنَّةَ قَاطِعٌ»، يعني قاطع رحم.",
        reference = "رواه مسلم 2556"
    ),
    IslamicFaqItem(
        question = "قريبي لا يتواصل معي، هل أتوقف أنا أيضًا؟",
        answer = "قال ﷺ:\n«وَلَكِنِ الْوَاصِلُ الَّذِي إِذَا قُطِعَتْ رَحِمُهُ وَصَلَهَا».",
        reference = "رواه البخاري"
    ),
    IslamicFaqItem(
        question = "أنا أحسن إلى قريبي لكنه يسيء إليّ، ماذا أفعل؟",
        answer = "قال رجل للنبي ﷺ إن له أقارب يصلهم ويقطعونه، ويحسن إليهم ويسيئون إليه، فقال ﷺ:\n«لَئِنْ كُنْتَ كَمَا قُلْتَ فَكَأَنَّمَا تُسِفُّهُمُ الْمَلَّ، وَلَا يَزَالُ مَعَكَ مِنَ اللَّهِ ظَهِيرٌ عَلَيْهِمْ مَا دُمْتَ عَلَى ذَلِكَ».",
        reference = "رواه مسلم"
    ),
    IslamicFaqItem(
        question = "ما علاقة الرحم برحمة الله؟",
        answer = "قال رسول الله ﷺ:\n«إِنَّ الرَّحِمَ شُجْنَةٌ مِنَ الرَّحْمَنِ، فَقَالَ اللَّهُ: أَلَا تَرْضَيْنَ أَنْ أَصِلَ مَنْ وَصَلَكِ، وَأَقْطَعَ مَنْ قَطَعَكِ؟»",
        reference = "رواه البخاري"
    ),
    IslamicFaqItem(
        question = "هل ورد أن الرحم تتعلق بالله وتستعيذ به من القطيعة؟",
        answer = "قال ﷺ إن الرحم قامت فقالت: «هَذَا مَقَامُ الْعَائِذِ بِكَ مِنَ الْقَطِيعَةِ»، فقال الله لها: «أَلَا تَرْضَيْنَ أَنْ أَصِلَ مَنْ وَصَلَكِ، وَأَقْطَعَ مَنْ قَطَعَكِ؟».",
        reference = "رواه البخاري"
    ),
    IslamicFaqItem(
        question = "لو تصدقت على قريب محتاج، هل لي أجر واحد أم أكثر؟",
        answer = "ورد في قصة زينب امرأة عبد الله بن مسعود رضي الله عنهما أن النبي ﷺ قال:\n«لَهُمَا أَجْرَانِ: أَجْرُ الْقَرَابَةِ وَأَجْرُ الصَّدَقَةِ».",
        reference = "متفق عليه"
    )
)

@Composable
fun IslamicFaqDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0E7075), Color(0xFF0D9E7B))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            text = "📖 أسئلة وأجوبة",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "من القرآن الكريم والسنة النبوية",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // FAQ list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    islamicFaqList.forEachIndexed { index, item ->
                        FaqItem(item = item, index = index + 1)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0E7075),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItem(item: IslamicFaqItem, index: Int) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Question row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Index badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0E7075).copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = index.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E7075)
                        )
                    }
                    Text(
                        text = item.question,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.weight(1f)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF0E7075),
                    modifier = Modifier.size(20.dp).padding(start = 4.dp)
                )
            }

            // Answer (animated)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                            RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = item.answer,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Right,
                        fontStyle = FontStyle.Normal
                    )
                    // Reference badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0E7075).copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.reference,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0E7075),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SilaHeaderBrush
import com.example.ui.theme.SoftGold

@Composable
fun CommitmentHeaderCard(
    commitmentPercentage: Int,
    isSyncingCallLogs: Boolean,
    onSyncCallLogsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Rotation animation for the sync button when syncing
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            .background(SilaHeaderBrush, shape = RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        "مَسيرَةُ صِلَتِكِ 🌸",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SoftGold,
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
                            "طوبى لكِ! صلة أرحامك تزيد في عمرك وتبارك رزقكِ. استمري في هذا العطاء الجميل ✨"
                        } else if (commitmentPercentage >= 40) {
                            "بداية طيبة ومباركة! بقي القليل من الأقارب بانتظار تواصلك معهم وبث الود في قلوبهم 💚"
                        } else {
                            "إن صلة الرحم معلقة بالعرش تقول: من وصلني وصله الله. ابدئي اليوم بخطوات بسيطة 🤍"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFFBCCEC3),
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Progress Arc Indicator
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
                            color = SoftGold,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Action Row: Auto Sync Call Logs Button
            Button(
                onClick = onSyncCallLogsClick,
                enabled = !isSyncingCallLogs,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftGold,
                    contentColor = Color(0xFF141816),
                    disabledContainerColor = SoftGold.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "مزامنة سجل المكالمات",
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isSyncingCallLogs) Modifier.rotate(angle) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSyncingCallLogs) "جاري رصد المكالمات تلقائياً..." else "مزامنة سجل المكالمات التلقائي 📞",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

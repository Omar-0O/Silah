package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftGold
import java.text.SimpleDateFormat
import java.util.*

/**
 * Simplified stats card showing:
 * - Total kin-tie contacts (all-time log count)
 * - Unique days where at least one contact was made
 * - Number of distinct relatives contacted
 */
@Composable
fun CommitmentHeaderCard(
    totalLogsCount: Int,
    uniqueDaysCount: Int,
    uniqueRelativesContacted: Int,
    lang: String = "ar",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF0E7075).copy(alpha = 0.3f))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0E7075), Color(0xFF0B565A), Color(0xFF084144))
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header title
            Text(
                text = if (lang == "en") "Your Kin-Tie Journey 🌿" else "مسيرة صلة الرحم 🌿",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SoftGold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Three stat chips in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    emoji = "🤝",
                    value = totalLogsCount.toString(),
                    label = if (lang == "en") "Total Contacts" else "إجمالي الصلات",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    emoji = "📅",
                    value = uniqueDaysCount.toString(),
                    label = if (lang == "en") "Days Connected" else "أيام الصلة",
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
            .background(Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFFD0E0D5),
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

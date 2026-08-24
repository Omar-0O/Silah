package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreyGreen
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeViewModel

import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

@Composable
fun DueRelativesCarousel(
    dueRelatives: List<Relative>,
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (dueRelatives.isNotEmpty()) Color(0xFFE57373) else Color(0xFF81C784))
            )
            Text(
                text = if (lang == "en") "Due to Connect ✨" else "حان وقتُ وَصْلِهِم ✨",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (dueRelatives.isNotEmpty()) {
                Badge(
                    containerColor = Color(0xFFE57373).copy(alpha = 0.2f),
                    contentColor = Color(0xFFD32F2F),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(" ${dueRelatives.size} ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (dueRelatives.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECF5F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF94DAB2).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = if (lang == "en") "All relatives are connected! 🎉" else "جميع أرحامكِ موصولون بالكامل! 🎉",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Text(
                            text = if (lang == "en") "Great job staying in touch with your family!" else "ما شاء الله، التزامكِ رائع ويقرب المسافات. طابت أيامكِ ببركة الود والرحمة.",
                            fontSize = 12.sp,
                            color = SecondaryGreyGreen
                        )
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
            ) {
                items(dueRelatives, key = { it.id }) { relative ->
                    val status = viewModel.getRelativeStatus(relative)
                    val statusBgColor = Color("#15${status.colorHex}".toColorInt())
                    val statusTextColor = Color("#FF${status.colorHex}".toColorInt())

                    Card(
                        modifier = Modifier
                            .width(280.dp)
                            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                            .clickable { viewModel.showLogsHistoryDialog.value = relative },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusBgColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (lang == "en") status.labelEn else status.label,
                                        color = statusTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = DateUtils.translateDegree(relative.relationshipDegree, lang),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RelativeAvatar(
                                        name = relative.name,
                                        photoUri = relative.photoUri,
                                        size = 36.dp,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = relative.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // WhatsApp Action
                                    IconButton(
                                        onClick = {
                                            var cleanPhone = relative.phone.replace("""[\s\-\(\)]""".toRegex(), "")
                                            val formattedPhone = when {
                                                cleanPhone.startsWith("+") -> cleanPhone.substring(1)
                                                cleanPhone.startsWith("00") -> cleanPhone.substring(2)
                                                cleanPhone.startsWith("01") && cleanPhone.length == 11 -> "20" + cleanPhone.substring(1)
                                                cleanPhone.startsWith("05") && cleanPhone.length == 10 -> "966" + cleanPhone.substring(1)
                                                cleanPhone.startsWith("0") -> cleanPhone.substring(1)
                                                else -> cleanPhone
                                            }
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = "https://api.whatsapp.com/send?phone=$formattedPhone".toUri()
                                            }
                                            context.startActivity(intent)
                                            val commType = if (lang == "en") "Message" else "رسالة"
                                            val note = if (lang == "en") "Quick WhatsApp message" else "تواصل سريع ومباشر عبر الواتساب"
                                            viewModel.recordCommunication(relative.id, commType, note)
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFFE8F5E9), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                                            contentDescription = if (lang == "en") "WhatsApp" else "واتساب",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Log Record Action
                                    IconButton(
                                        onClick = { viewModel.showRecordLogDialog.value = relative },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = if (lang == "en") "Log" else "سجل",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (lang == "en")
                                    "Last: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)}"
                                else
                                    "آخر تواصل: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}


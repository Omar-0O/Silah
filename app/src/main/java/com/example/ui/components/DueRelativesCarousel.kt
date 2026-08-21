package com.example.ui.components

import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.DateUtils

@Composable
fun DueRelativesCarousel(
    dueRelatives: List<Relative>,
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                text = "حان وقتُ وَصْلِهِم ✨",
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
                            "جميع أرحامكِ موصولون بالكامل! 🎉",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Text(
                            "ما شاء الله، التزامكِ رائع ويقرب المسافات. طابت أيامكِ ببركة الود والرحمة.",
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
                items(dueRelatives) { relative ->
                    val status = viewModel.getRelativeStatus(relative)
                    val statusBgColor = Color(android.graphics.Color.parseColor("#15" + status.colorHex))
                    val statusTextColor = Color(android.graphics.Color.parseColor("#FF" + status.colorHex))

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
                                        text = status.label,
                                        color = statusTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = relative.relationshipDegree,
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
                                Text(
                                    text = relative.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // WhatsApp Action
                                    IconButton(
                                        onClick = {
                                            var cleanPhone = relative.phone.replace("\\s|-|\\(|\\)".toRegex(), "")
                                            if (!cleanPhone.startsWith("+") && !cleanPhone.startsWith("00")) {
                                                if (cleanPhone.startsWith("0")) {
                                                    cleanPhone = "966" + cleanPhone.substring(1)
                                                }
                                            }
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                                            }
                                            context.startActivity(intent)
                                            viewModel.recordCommunication(relative.id, "رسالة", "تواصل سريع ومباشر عبر الواتساب")
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFFE8F5E9), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Chat,
                                            contentDescription = "واتساب",
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
                                            contentDescription = "سجل",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "آخر تواصل: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate)}",
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


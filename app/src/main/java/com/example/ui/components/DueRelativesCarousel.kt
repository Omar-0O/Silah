package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeViewModel
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

@Composable
fun DueRelativesCarousel(
    dueRelatives: List<Relative>,
    viewModel: RelativeViewModel,
    totalRelativesCount: Int = 0,
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
                    .background(
                        when {
                            totalRelativesCount == 0 -> SoftGold
                            dueRelatives.isNotEmpty() -> Color(0xFFE57373)
                            else -> Color(0xFF81C784)
                        }
                    )
            )
            Text(
                text = if (totalRelativesCount == 0) (if (lang == "en") "Start Kinship Connection 🌿" else "ابدأ صِلَة أرحامك 🌿")
                       else (if (lang == "en") "Due to Connect ✨" else "حان وقتُ وَصْلِهِم ✨"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (totalRelativesCount > 0 && dueRelatives.isNotEmpty()) {
                Badge(
                    containerColor = Color(0xFFE57373).copy(alpha = 0.2f),
                    contentColor = Color(0xFFD32F2F),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(" ${dueRelatives.size} ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        when {
            totalRelativesCount == 0 -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showAddRelativeDialog.value = true }
                        .border(1.dp, SoftGold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "en") "No relatives added yet 🌿" else "لم تقم بإضافة أقارب بعد 🌿",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (lang == "en") "Tap here to add your first relative and start organizing your family ties."
                                       else "اضغط هنا لإضافة أول قريب، وتبدأ بتوثيق صِلَة أرحامك ومتابعتهم بكل يُسْر.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            dueRelatives.isEmpty() -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Column {
                            Text(
                                text = if (lang == "en") "All relatives are connected! 🎉" else "جميع أرحامك موصولون بالكامل! 🎉",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (lang == "en") "Great job staying in touch with your family!" else "ما شاء الله، التزامك رائع ويقرب المسافات. طابت أيامك ببركة الود والرحمة.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    items(dueRelatives, key = { relative -> "${relative.id}_${relative.name}_${relative.phone}" }) { relative ->
                        val status = viewModel.getRelativeStatus(relative)
                        val statusBgColor = status.color.copy(alpha = 0.15f)
                        val statusTextColor = status.color


                        Card(
                            modifier = Modifier
                                .width(285.dp)
                                .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
                                .clickable { viewModel.showLogsHistoryDialog.value = relative },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // 1. Status & Degree Header
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
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. Relative Avatar, Name & Last Contact Date
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RelativeAvatar(
                                        name = relative.name,
                                        photoUri = relative.photoUri,
                                        size = 44.dp,
                                        fontSize = 18.sp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = relative.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (lang == "en")
                                                "Last: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)}"
                                            else
                                                "آخر تواصل: ${DateUtils.formatRelativeTimeExact(relative.lastContactDate, lang)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 3. Action Buttons Row (Call, WhatsApp, Log)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Call Action
                                    Surface(
                                        onClick = {
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${relative.phone}")
                                                })
                                            } catch (e: Exception) {
                                                Toast.makeText(context, if (lang == "en") "Unable to open dialer" else "تعذر فتح لوحة الاتصال", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Call,
                                                contentDescription = if (lang == "en") "Call" else "اتصال",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "en") "Call" else "اتصال",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // WhatsApp Action
                                    Surface(
                                        onClick = {
                                            try {
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
                                            } catch (e: Exception) {
                                                Toast.makeText(context, if (lang == "en") "WhatsApp not installed" else "تطبيق الواتساب غير مثبت على الجهاز", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF1B8A4A),
                                        contentColor = Color.White,
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                                contentDescription = if (lang == "en") "WhatsApp" else "واتس",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "en") "WhatsApp" else "واتس",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Log Record Action
                                    Surface(
                                        onClick = { viewModel.showRecordLogDialog.value = relative },
                                        shape = RoundedCornerShape(12.dp),
                                        color = SoftGold.copy(alpha = 0.25f),
                                        contentColor = Color(0xFF7A5200),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = if (lang == "en") "Log" else "سجّل",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (lang == "en") "Log" else "سجّل",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

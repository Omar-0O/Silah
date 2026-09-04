package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.example.data.Relative
import com.example.ui.components.RelativeAvatar
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.utils.DateUtils
import com.example.viewmodel.RelativeViewModel

/**
 * Comfortable Soft-UI popup dialog shown upon entering the app
 * asking whether the user has connected with relatives that were notified today.
 */
@Composable
fun KinshipCheckInDialog(
    relatives: List<Relative>,
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    if (relatives.isEmpty()) return

    val context = LocalContext.current
    val lang by viewModel.selectedLanguage.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Top Header Row (Icon + Close) ───────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Soft Glowing Icon Box
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryGreen.copy(alpha = 0.20f),
                                        PrimaryGreen.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(1.dp, PrimaryGreen.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌸", fontSize = 24.sp)
                    }

                    // Close icon button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = if (lang == "en") "Close" else "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Dialog Title & Subtitle ─────────────────────────────────
                Text(
                    text = if (lang == "en") "Did you reach out today? 🌸" else "هل تواصلت مع أحبابك اليوم؟ 🌸",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (lang == "en")
                        "You received a reminder today to connect with these relatives. Let us know if you stayed in touch!"
                    else
                        "أرسلنا لك تذكيراً اليوم للتواصل مع هؤلاء الأقارب الأعزاء. طمئنا هل تم الوصل؟",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ── List of Pending Notified Relatives ───────────────────────
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(relatives, key = { it.id }) { relative ->
                        CheckInRelativeCard(
                            relative = relative,
                            lang = lang,
                            onCall = {
                                if (relative.phone.isNotBlank()) {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${relative.phone}")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(dialIntent)
                                }
                            },
                            onWhatsApp = {
                                if (relative.phone.isNotBlank()) {
                                    val cleanPhone = relative.phone.replace("""[\s\-\(\)\+]""".toRegex(), "")
                                    val formattedPhone = when {
                                        cleanPhone.startsWith("00") -> cleanPhone.substring(2)
                                        cleanPhone.startsWith("01") && cleanPhone.length == 11 -> "20" + cleanPhone.substring(1)
                                        cleanPhone.startsWith("05") && cleanPhone.length == 10 -> "966" + cleanPhone.substring(1)
                                        cleanPhone.startsWith("0") -> cleanPhone.substring(1)
                                        else -> cleanPhone
                                    }
                                    val waIntent = Intent(Intent.ACTION_VIEW, "https://api.whatsapp.com/send?phone=$formattedPhone".toUri()).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(waIntent)
                                    } catch (e: Exception) {
                                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${relative.phone}")).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        try { context.startActivity(smsIntent) } catch (ex: Exception) { ex.printStackTrace() }
                                    }
                                }
                            },
                            onConfirmContacted = {
                                viewModel.confirmContactedFromDialog(relative.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Bottom Action Buttons ───────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // If multiple relatives, offer bulk confirmation
                    if (relatives.size > 1) {
                        Button(
                            onClick = { viewModel.confirmAllContactedFromDialog() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (lang == "en") "Yes, reached out to all 💚" else "نعم، تواصلت مع الجميع 💚",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Secondary dismiss button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (lang == "en") "I'll connect with them later ⏳" else "سأتواصل معهم لاحقاً ⏳",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual relative item in the check-in dialog.
 */
@Composable
private fun CheckInRelativeCard(
    relative: Relative,
    lang: String,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onConfirmContacted: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftGold.copy(alpha = 0.20f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Relative Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RelativeAvatar(
                    name = relative.name,
                    photoUri = relative.photoUri,
                    size = 40.dp,
                    fontSize = 16.sp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = relative.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = DateUtils.translateDegree(relative.relationshipDegree, lang),
                        fontSize = 11.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Actions Row (Call, WhatsApp, Mark Contacted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call button
                Surface(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(0.9f).height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (lang == "en") "Call" else "اتصال",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // WhatsApp button
                Surface(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1B8A4A).copy(alpha = 0.15f),
                    contentColor = Color(0xFF1B8A4A),
                    modifier = Modifier.weight(0.9f).height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (lang == "en") "WhatsApp" else "واتس",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Confirm Contacted button
                Button(
                    onClick = onConfirmContacted,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1.4f).height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (lang == "en") "Yes, Connected ✅" else "نعم، تواصلت ✅",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

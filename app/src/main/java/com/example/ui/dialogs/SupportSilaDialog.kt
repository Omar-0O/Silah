package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SupportConfig
import com.example.utils.QRCodeUtils

enum class SupportPage { MAIN, INSTAPAY, VODAFONE_CASH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSilaDialog(
    contactedCount: Int,
    interactionCount: Int,
    daysUsingApp: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(SupportPage.MAIN) }
    var showAppreciationBanner by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints {
            val isTablet = maxWidth > 600.dp
            val dialogWidth = if (isTablet) 540.dp else maxWidth * 0.92f
            val dialogMaxHeight = if (maxHeight > 800.dp) 0.85f else 0.92f

            Surface(
                modifier = Modifier
                    .width(dialogWidth)
                    .fillMaxHeight(dialogMaxHeight)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Top Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentPage != SupportPage.MAIN) {
                                IconButton(
                                    onClick = { currentPage = SupportPage.MAIN },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "الرجوع",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = when (currentPage) {
                                    SupportPage.MAIN -> "ادعم صِلَةِ"
                                    SupportPage.INSTAPAY -> "الدعم عبر InstaPay"
                                    SupportPage.VODAFONE_CASH -> "الدعم عبر المحفظة الإلكترونية"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Appreciation Banner
                    AnimatedVisibility(
                        visible = showAppreciationBanner,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = "شكرًا لدعمك لصِلَةِ! جزاك الله خيراً وبارك في رزقك.",
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                // Screen Switcher
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SupportPageTransition"
                ) { page ->
                    when (page) {
                        SupportPage.MAIN -> MainSupportContent(
                            contactedCount = contactedCount,
                            interactionCount = interactionCount,
                            daysUsingApp = daysUsingApp,
                            onSelectInstaPay = { currentPage = SupportPage.INSTAPAY },
                            onSelectVodafone = { currentPage = SupportPage.VODAFONE_CASH }
                        )

                        SupportPage.INSTAPAY -> DedicatedInstaPayPage(
                            context = context,
                            onActionDone = { showAppreciationBanner = true }
                        )

                        SupportPage.VODAFONE_CASH -> DedicatedVodafoneCashPage(
                            context = context,
                            onActionDone = { showAppreciationBanner = true }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun MainSupportContent(
    contactedCount: Int,
    interactionCount: Int,
    daysUsingApp: Long,
    onSelectInstaPay: () -> Unit,
    onSelectVodafone: () -> Unit
) {
    Column {
        // Top Banner - Prominent Free & Ad-Free Statement
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0F2F1)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "✨", fontSize = 18.sp)
                    Text(
                        text = "صِلَةِ مجاني 100% وبدون إعلانات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تطبيق صِلَةِ بدون أي اشتراكات. دعمك الاختياري يساهم مباشرة في استمرار تطوير وتحديث التطبيق.",
                    fontSize = 12.sp,
                    color = Color(0xFF00796B),
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal Impact Section
        Text(
            text = "أثرك في صِلَةِ 📊",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImpactStatCard(
                modifier = Modifier.weight(1f),
                icon = "👥",
                value = contactedCount.toString(),
                label = "أقارب تواصلت معهم"
            )
            ImpactStatCard(
                modifier = Modifier.weight(1f),
                icon = "📝",
                value = interactionCount.toString(),
                label = "تفاعلات مسجلة"
            )
            ImpactStatCard(
                modifier = Modifier.weight(1f),
                icon = "⏳",
                value = maxOf(1L, daysUsingApp).toString(),
                label = "أيام استخدام"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Support Methods Selection
        Text(
            text = "اختر طريقة الدعم 💳",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Method 1 Button Card
        SupportMethodNavCard(
            title = "InstaPay (إنستا باي)",
            subtitle = "تحويل مباشر وسريع عبر عنوان IPA",
            iconText = "📲",
            onClick = onSelectInstaPay
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Method 2 Button Card
        SupportMethodNavCard(
            title = "المحفظة الإلكترونية (فودافون كاش)",
            subtitle = "تحويل من فودافون/اتصالات/أورنج كاش أو محفظة بنكية",
            iconText = "📱",
            onClick = onSelectVodafone
        )
    }
}

@Composable
private fun DedicatedInstaPayPage(
    context: Context,
    onActionDone: () -> Unit
) {
    var showQR by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Address Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0FDF4)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📲", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "عنوان الدفع الخاص بـ InstaPay (IPA)",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = SupportConfig.INSTAPAY_ADDRESS,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF14532D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy Address
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("InstaPay IPA", SupportConfig.INSTAPAY_ADDRESS)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ عنوان InstaPay بنجاح ✨", Toast.LENGTH_SHORT).show()
                            onActionDone()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ العنوان", fontSize = 13.sp)
                    }

                    // Open InstaPay App
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SupportConfig.INSTAPAY_LINK))
                                context.startActivity(intent)
                                onActionDone()
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح تطبيق InstaPay تلقائياً", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح التطبيق", fontSize = 13.sp)
                    }

                    // Toggle QR
                    IconButton(
                        onClick = { showQR = !showQR },
                        modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color(0xFF166534))
                    }
                }

                // QR Code View
                AnimatedVisibility(visible = showQR) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val qrBitmap = remember { QRCodeUtils.generateQRCode(SupportConfig.INSTAPAY_LINK, 400) }
                        if (qrBitmap != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.size(170.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed Step-by-Step Guide
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "خطوات التحويل بالتفصيل 📋",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                StepGuideItem(
                    stepNumber = "1",
                    title = "افتح تطبيق InstaPay",
                    description = "قم بفتح تطبيق إنستا باي على هاتفك واضغط على زر الإرسال."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                StepGuideItem(
                    stepNumber = "2",
                    title = "اختر الإرسال عبر عنوان IPA",
                    description = "اختر \"إرسال نقود\" ثم حدد الخيار \"عنوان الدفع (IPA)\"."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                StepGuideItem(
                    stepNumber = "3",
                    title = "الصق عنوان الدفع",
                    description = "أدخل العنوان: ${SupportConfig.INSTAPAY_ADDRESS} (أو اضغط زر نسخ العنوان أعلاه)."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                StepGuideItem(
                    stepNumber = "4",
                    title = "حدد المبلغ واضغط تأكيد",
                    description = "اكتب مبلغ التبرع الاختياري وأدخل الرقم السري لـ InstaPay لإتمام التحويل."
                )
            }
        }
    }
}

@Composable
private fun DedicatedVodafoneCashPage(
    context: Context,
    onActionDone: () -> Unit
) {
    var showQR by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Phone/Wallet Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF2F2)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "رقم المحفظة الإلكترونية (فودافون كاش)",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = SupportConfig.VODAFONE_CASH_NUMBER,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Copy Number
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Vodafone Cash Number", SupportConfig.VODAFONE_CASH_NUMBER)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الرقم بنجاح ✨", Toast.LENGTH_SHORT).show()
                            onActionDone()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ الرقم", fontSize = 13.sp)
                    }

                    // Direct USSD Call *9#
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*9%23"))
                                context.startActivity(intent)
                                onActionDone()
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر فتح لوحة الاتصال", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طلب *9#", fontSize = 13.sp)
                    }

                    // Toggle QR
                    IconButton(
                        onClick = { showQR = !showQR },
                        modifier = Modifier.background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color(0xFF991B1B))
                    }
                }

                // QR Code View
                AnimatedVisibility(visible = showQR) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val qrBitmap = remember { QRCodeUtils.generateQRCode(SupportConfig.VODAFONE_CASH_NUMBER, 400) }
                        if (qrBitmap != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp),
                                modifier = Modifier.size(170.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed Step-by-Step Guide
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "خطوات التحويل بالتفصيل 📋",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                StepGuideItem(
                    stepNumber = "1",
                    title = "افتح كود المحفظة أو التطبيق",
                    description = "اطلب كود *9# (فودافون كاش) أو افتح تطبيق المحفظة الخاص بك (أورنج كاش، اتصالات، WE، محفظة البنك)."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                StepGuideItem(
                    stepNumber = "2",
                    title = "اختر تحويل الأموال",
                    description = "حدد خيار \"تحويل الأموال\" ثم اختر الإرسال إلى رقم آخر."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                StepGuideItem(
                    stepNumber = "3",
                    title = "أدخل الرقم المستلم",
                    description = "اكتب الرقم: ${SupportConfig.VODAFONE_CASH_NUMBER} (أو استخدم زر نسخ الرقم أعلاه)."
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                StepGuideItem(
                    stepNumber = "4",
                    title = "حدد المبلغ والرقم السري",
                    description = "أدخل مبلغ التبرع الاختياري ثم أدخل الرقم السري للمحفظة لتأكيد التحويل."
                )
            }
        }
    }
}

@Composable
private fun StepGuideItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun SupportMethodNavCard(
    title: String,
    subtitle: String,
    iconText: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Text(text = iconText, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ImpactStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}


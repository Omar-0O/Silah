package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.InstapayConfig
import com.example.data.SupportConfig
import com.example.data.WalletMethod
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SoftGold
import com.example.utils.QRCodeUtils
import com.example.viewmodel.RelativeViewModel

@Composable
fun SupportSilaDialog(
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val relatives by viewModel.relatives.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val usageDays by viewModel.appUsageDays.collectAsState()

    val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
    val context = LocalContext.current

    val supportConfig = remember { SupportConfig.DEFAULT }

    var selectedSupportMethod by remember { mutableStateOf<String?>(null) } // "instapay" or "wallet"
    var showThankYouMessage by remember { mutableStateOf(false) }

    val contactedRelativesCount = remember(relatives) {
        relatives.count { it.lastContactDate > 0L }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedLanguage == "en") "🤍 Support Sila" else "🤍 ادعم صِلَةِ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close")
                        }
                    }

                    // Main App Core Philosophy Box
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (selectedLanguage == "en")
                                    "Sila is free, ad-free, and subscription-free."
                                else
                                    "صِلَة مجاني ومن غير إعلانات أو اشتراكات.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            Text(
                                text = if (selectedLanguage == "en")
                                    "If you like the application and wish to help me continue developing it, you can support with any amount that suits you.\n\nYour support is completely optional and helps me keep developing Sila and adding new features."
                                else
                                    "لو التطبيق عجبك وحابب تساعدني أكمل تطويره، تقدر تدعمني بالمبلغ اللي يناسبك.\n\nدعمك اختياري بالكامل، وبيساعدني أكمل تطوير صِلَة وإضافة مميزات جديدة.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryGreen.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selectedLanguage == "en")
                                        "Sila is free, and your support is completely optional and helps me keep developing it. 🤍"
                                    else
                                        "صِلَة مجاني، ودعمك اختياري ويساعدني أكمل تطويره. 🤍",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryGreen,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Personal Impact Section (Real application data only)
                    Text(
                        text = if (selectedLanguage == "en") "🤍 Your Sila Impact" else "🤍 أثرك في صِلَةِ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Impact Card 1: Relatives Contacted
                        ImpactStatCard(
                            modifier = Modifier.weight(1f),
                            emoji = "👥",
                            value = "$contactedRelativesCount",
                            label = if (selectedLanguage == "en") "Relatives Contacted" else "أقارب تواصلت معهم"
                        )
                        // Impact Card 2: Total Interactions
                        ImpactStatCard(
                            modifier = Modifier.weight(1f),
                            emoji = "📝",
                            value = "${logs.size}",
                            label = if (selectedLanguage == "en") "Interactions Recorded" else "تفاعل مسجل"
                        )
                        // Impact Card 3: Time Using Sila
                        ImpactStatCard(
                            modifier = Modifier.weight(1f),
                            emoji = "⏳",
                            value = "$usageDays",
                            label = if (selectedLanguage == "en") "Days using Sila" else "يوم مع صِلَة"
                        )
                    }

                    HorizontalDivider()

                    // Choose Support Method Header
                    Text(
                        text = if (selectedLanguage == "en") "Choose a Support Method" else "اختر طريقة الدعم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Method 1: InstaPay
                    if (supportConfig.instapay.enabled) {
                        SupportMethodOptionCard(
                            title = if (selectedLanguage == "en") "📲 InstaPay" else "📲 InstaPay",
                            description = if (selectedLanguage == "en")
                                "Direct and instant transfer via InstaPay."
                            else
                                "تحويل مباشر وسريع عبر InstaPay.",
                            badgeText = "InstaPay",
                            onClick = { selectedSupportMethod = "instapay" }
                        )
                    } else {
                        DisabledSupportMethodCard(
                            title = if (selectedLanguage == "en") "📲 InstaPay" else "📲 InstaPay",
                            selectedLanguage = selectedLanguage
                        )
                    }

                    // Method 2: Mobile Wallet
                    val enabledWallet = supportConfig.wallets.firstOrNull { it.enabled }
                    if (enabledWallet != null) {
                        SupportMethodOptionCard(
                            title = if (selectedLanguage == "en") "📱 Mobile Wallet (${enabledWallet.name})" else "📱 محفظة إلكترونية (${enabledWallet.nameAr})",
                            description = if (selectedLanguage == "en")
                                "Support Sila using your mobile wallet (${enabledWallet.name})."
                            else
                                "ادعم صِلَة من خلال محفظتك الإلكترونية (${enabledWallet.nameAr}).",
                            badgeText = enabledWallet.name,
                            onClick = { selectedSupportMethod = "wallet" }
                        )
                    } else {
                        DisabledSupportMethodCard(
                            title = if (selectedLanguage == "en") "📱 Mobile Wallet" else "📱 المحفظة الإلكترونية",
                            selectedLanguage = selectedLanguage
                        )
                    }

                    // Simple appreciation banner if copied/opened link
                    if (showThankYouMessage) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SoftGold.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, SoftGold)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedLanguage == "en") "🤍 Thank you for supporting Sila!" else "🤍 شكرًا لدعمك لصِلَة",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(
                            text = if (selectedLanguage == "en") "Close" else "إغلاق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Modal / Bottom Sheet for InstaPay Flow
    if (selectedSupportMethod == "instapay") {
        InstaPaySupportModal(
            config = supportConfig.instapay,
            lang = selectedLanguage,
            onDismiss = { selectedSupportMethod = null },
            onActionDone = { showThankYouMessage = true }
        )
    }

    // Modal / Bottom Sheet for Mobile Wallet Flow
    if (selectedSupportMethod == "wallet" && supportConfig.wallets.any { it.enabled }) {
        val wallet = supportConfig.wallets.first { it.enabled }
        WalletSupportModal(
            wallet = wallet,
            lang = selectedLanguage,
            onDismiss = { selectedSupportMethod = null },
            onActionDone = { showThankYouMessage = true }
        )
    }
}

@Composable
private fun ImpactStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
            Text(
                label,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SupportMethodOptionCard(
    title: String,
    description: String,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DisabledSupportMethodCard(
    title: String,
    selectedLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (selectedLanguage == "en") "Currently Unavailable" else "طريقة الدعم دي غير متاحة حاليًا.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun InstaPaySupportModal(
    config: InstapayConfig,
    lang: String,
    onDismiss: () -> Unit,
    onActionDone: () -> Unit
) {
    val context = LocalContext.current
    var selectedAmount by remember { mutableStateOf("50 EGP") }
    var customAmount by remember { mutableStateOf("") }
    val layoutDirection = if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    val qrBitmap = remember(config.paymentLink) {
        QRCodeUtils.generateQRCodeBitmap(config.paymentLink)
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == "en") "Support via InstaPay 📲" else "الدعم عبر InstaPay 📲",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )

                    // Choose Amount Section
                    Text(
                        text = if (lang == "en") "Select Amount (Optional)" else "اختر المبلغ (اختياري)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val amounts = listOf("10 EGP", "50 EGP", "100 EGP", "500 EGP", "مبلغ آخر")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        amounts.forEach { amt ->
                            val isSelected = selectedAmount == amt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAmount = amt },
                                label = { Text(amt, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (selectedAmount == "مبلغ آخر") {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it },
                            label = { Text(if (lang == "en") "Custom Amount (EGP)" else "المبلغ بالجنيه") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    // IPA Box
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "InstaPay IPA",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = config.ipa,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, "InstaPay IPA", config.ipa)
                                    Toast.makeText(context, if (lang == "en") "IPA Copied!" else "تم نسخ العنوان!", Toast.LENGTH_SHORT).show()
                                    onActionDone()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "en") "Copy IPA" else "نسخ IPA", fontSize = 12.sp)
                            }
                        }
                    }

                    // Direct Payment Link Button
                    Button(
                        onClick = {
                            openUrlInBrowser(context, config.paymentLink)
                            onActionDone()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(Icons.Outlined.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == "en") "Open InstaPay Link" else "فتح رابط InstaPay", fontWeight = FontWeight.Bold)
                    }

                    // QR Code
                    if (qrBitmap != null) {
                        Text(
                            text = if (lang == "en") "Scan QR Code with InstaPay app" else "امسح الـ QR Code من تطبيق InstaPay",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "InstaPay QR Code",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(if (lang == "en") "Close" else "إغلاق", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletSupportModal(
    wallet: WalletMethod,
    lang: String,
    onDismiss: () -> Unit,
    onActionDone: () -> Unit
) {
    val context = LocalContext.current
    var selectedAmount by remember { mutableStateOf("50 EGP") }
    var customAmount by remember { mutableStateOf("") }
    val layoutDirection = if (lang == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    val qrBitmap = remember(wallet.phoneNumber) {
        QRCodeUtils.generateQRCodeBitmap(wallet.phoneNumber)
    }

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == "en") "Support via ${wallet.name} 📱" else "الدعم عبر ${wallet.nameAr} 📱",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )

                    // Choose Amount Section
                    Text(
                        text = if (lang == "en") "Select Amount (Optional)" else "اختر المبلغ (اختياري)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val amounts = listOf("10 EGP", "50 EGP", "100 EGP", "500 EGP", "مبلغ آخر")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        amounts.forEach { amt ->
                            val isSelected = selectedAmount == amt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAmount = amt },
                                label = { Text(amt, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (selectedAmount == "مبلغ آخر") {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it },
                            label = { Text(if (lang == "en") "Custom Amount (EGP)" else "المبلغ بالجنيه") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    // Wallet Phone Box
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (lang == "en") "${wallet.name} Number" else "رقم محفظة ${wallet.nameAr}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = wallet.phoneNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            Button(
                                onClick = {
                                    copyToClipboard(context, wallet.name, wallet.phoneNumber)
                                    Toast.makeText(context, if (lang == "en") "Number Copied!" else "تم نسخ الرقم!", Toast.LENGTH_SHORT).show()
                                    onActionDone()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "en") "Copy Number" else "نسخ الرقم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // QR Code
                    if (qrBitmap != null) {
                        Text(
                            text = if (lang == "en") "Scan QR Code for wallet transfer" else "امسح الـ QR Code للتحويل للمحفظة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "Wallet QR Code",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(if (lang == "en") "Close" else "إغلاق", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

private fun openUrlInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

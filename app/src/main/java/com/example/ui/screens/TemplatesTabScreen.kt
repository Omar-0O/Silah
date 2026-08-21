package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuickTemplate
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesTabScreen(
    viewModel: RelativeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val templates by viewModel.templates.collectAsState()
    var selectedOccasion by remember { mutableStateOf("الكل") }

    val defaultTemplates = remember {
        listOf(
            QuickTemplate(1, "تهنئة يوم الجمعة ✨", "السلام عليكم ورحمة الله وبركاته. في هذا اليوم المبارك أسأل الله أن يبارك في عمركم وصحتكم ويتقبل طاعاتكم. جمعة مباركة وطيبة 🌸", "جمعة"),
            QuickTemplate(2, "اطمئنان دافئ 🤍", "السلام عليكم ورحمة الله. أردت الاطمئنان على صحتكم وأحوالكم، عساكم بألف خير ونعمة دائماً. مشتاقون لرؤيتكم قريبًا.", "عام"),
            QuickTemplate(3, "تهنئة بالأعياد والمناسبات 🌙", "أصدق التهاني وأطيب التبريكات بمناسبة حلول العيد المبارك، سائلاً المولى عز وجل أن يعيده علينا وعليكم بالخير واليمن والبركات.", "مناسبات"),
            QuickTemplate(4, "دعاء بالشفاء والعافية 🤲", "طهور ونور إن شاء الله. أسأل الله العظيم رب العرش العظيم أن يلبسكم ثوب الصحة والعافية الشاملة ولا يريكم مكروهاً.", "دعاء")
        )
    }

    val displayTemplates = if (templates.isEmpty()) defaultTemplates else templates
    val occasions = listOf("الكل", "جمعة", "عام", "مناسبات", "دعاء")

    val filteredList = displayTemplates.filter {
        selectedOccasion == "الكل" || it.category == selectedOccasion
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "قَوالِبُ الرَّسَائِلِ الجَاهِزَةِ ✉️",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(occasions) { category ->
                        val isSelected = selectedOccasion == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedOccasion = category },
                            label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            items(filteredList) { template ->
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.03f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = template.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(template.category, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = template.content,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy Text Button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(template.content))
                                    Toast.makeText(context, "تم نسخ النص للحافظة! 📋", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Share Button
                            Button(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, template.content)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "إرسال قالب صِلَةِ عبر:")
                                    context.startActivity(shareIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال عبر الواتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

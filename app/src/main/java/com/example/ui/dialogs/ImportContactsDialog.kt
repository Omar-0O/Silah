package com.example.ui.dialogs

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CallLogManager
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContactsDialog(
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val contacts by viewModel.deviceContacts.collectAsState()
    val isLoading by viewModel.isLoadingContacts.collectAsState()
    val existingRelatives by viewModel.relatives.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedContactPhone by remember { mutableStateOf<String?>(null) }

    // Existing normalized numbers to avoid double addition
    val existingNormalizedPhones = remember(existingRelatives) {
        existingRelatives.map { CallLogManager.normalizePhoneNumber(it.phone) }.toSet()
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { contact ->
            contact.name.contains(searchQuery, ignoreCase = true) || contact.phone.contains(searchQuery)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header Title
                Text(
                    text = "استيراد الأقارب من الهاتف 📲",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "حدد أرحامك واضبط تكرار التذكير لكل قريب بسهولة",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Explanatory Banner (User Flow Guidance)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 يكتشف التطبيق مكالماتك تلقائياً مع أقاربك عبر سجل الهاتف، ويمكنك أيضاً تسجيل التواصل يدوياً في أي وقت.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث بالاسم أو رقم الهاتف...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("جاري قراءة جهات الاتصال...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                } else if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا يوجد جهات اتصال مطابقة للبحث" else "لم يتم العثور على جهات اتصال في الهاتف",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            val name = contact.name
                            val phone = contact.phone
                            val photoUri = contact.photoUri
                            val normalized = CallLogManager.normalizePhoneNumber(phone)
                            val isAlreadyAdded = existingNormalizedPhones.contains(normalized)
                            val isExpanded = selectedContactPhone == phone

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isAlreadyAdded) {
                                        selectedContactPhone = if (isExpanded) null else phone
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAlreadyAdded)
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    else if (isExpanded)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            com.example.ui.components.RelativeAvatar(
                                                name = name,
                                                photoUri = photoUri,
                                                size = 38.dp,
                                                fontSize = 15.sp
                                            )

                                            Column {
                                                Text(
                                                    text = name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isAlreadyAdded) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = phone,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }

                                        if (isAlreadyAdded) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("مضاف 🟢", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text(
                                                text = if (isExpanded) "إغلاق ▲" else "تحديد التذكير ＋",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Inline Setup Form when contact is tapped
                                    AnimatedVisibility(visible = isExpanded && !isAlreadyAdded) {
                                        ContactSetupInlineForm(
                                            initialName = name,
                                            initialPhone = phone,
                                            viewModel = viewModel,
                                            onSave = { degree, interval ->
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                viewModel.addRelative(
                                                    name = name,
                                                    phone = phone,
                                                    relationshipDegree = degree,
                                                    intervalDays = interval,
                                                    notes = "تم استيراده من جهات الاتصال",
                                                    photoUri = photoUri
                                                )
                                                Toast.makeText(context, "تمت إضافة $name بنجاح في صِلَةِ! ✨", Toast.LENGTH_SHORT).show()
                                                selectedContactPhone = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Dismiss Action
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تم والعودة للقائمة 🌸", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContactSetupInlineForm(
    initialName: String,
    initialPhone: String,
    viewModel: RelativeViewModel,
    onSave: (degree: String, intervalDays: Int) -> Unit
) {
    var relationshipDegree by remember { mutableStateOf(viewModel.suggestRelationshipDegree(initialName)) }
    var intervalDays by remember { mutableIntStateOf(7) }

    val degrees = listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val intervals = listOf(
        Pair(1, "يومياً"),
        Pair(3, "كل 3 أيام"),
        Pair(7, "كل أسبوع"),
        Pair(14, "كل أسبوعين"),
        Pair(30, "كل شهر")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("درجة القرابة:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(degrees) { degree ->
                FilterChip(
                    selected = relationshipDegree == degree,
                    onClick = { relationshipDegree = degree },
                    label = { Text(degree, fontSize = 10.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Text("موعد التذكير الدوري:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(intervals) { (days, label) ->
                FilterChip(
                    selected = intervalDays == days,
                    onClick = { intervalDays = days },
                    label = { Text(label, fontSize = 10.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Button(
            onClick = { onSave(relationshipDegree, intervalDays) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ وتفعيل التذكير ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
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
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    var searchQuery by remember { mutableStateOf("") }
    var selectedContactPhone by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchDeviceContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.fetchDeviceContacts(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

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
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
                        text = if (selectedLanguage == "en") "Import Relatives from Phone 📲" else "استيراد الأقارب من الهاتف 📲",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (selectedLanguage == "en") "Select your relatives and set reminder intervals easily" else "حدد أرحامك واضبط تكرار التذكير لكل قريب بسهولة",
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
                            text = if (selectedLanguage == "en") "💡 The app automatically detects calls with your relatives via phone log, and you can also record contact manually anytime."
                                   else "💡 يكتشف التطبيق مكالماتك تلقائياً مع أقاربك عبر سجل الهاتف، ويمكنك أيضاً تسجيل التواصل يدوياً في أي وقت.",
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
                        placeholder = { Text(if (selectedLanguage == "en") "Search name or phone number..." else "ابحث بالاسم أو رقم الهاتف...", fontSize = 12.sp) },
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
                                Text(if (selectedLanguage == "en") "Loading contacts..." else "جاري قراءة جهات الاتصال...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    } else if (filteredContacts.isEmpty()) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = if (!hasPermission) {
                                        if (selectedLanguage == "en") "Permission required to access contacts 📱"
                                        else "يلزم السماح بالوصول لجهات الاتصال لانتخاب أرحامك 📱"
                                    } else if (searchQuery.isNotEmpty()) {
                                        if (selectedLanguage == "en") "No matching contacts found"
                                        else "لا يوجد جهات اتصال مطابقة للبحث"
                                    } else {
                                        if (selectedLanguage == "en") "No contacts found on device"
                                        else "لم يتم العثور على جهات اتصال في الهاتف"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (!hasPermission) {
                                    Button(
                                        onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (selectedLanguage == "en") "Grant Contacts Permission 📱" else "منح صلاحية جهات الاتصال 📱",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (contacts.isEmpty()) {
                                    OutlinedButton(
                                        onClick = { viewModel.fetchDeviceContacts(context) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (selectedLanguage == "en") "Refresh Contacts 🔄" else "تحديث جهات الاتصال 🔄",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
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
                                                    Text(if (selectedLanguage == "en") "Added 🟢" else "مضاف 🟢", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Text(
                                                    text = if (isExpanded) (if (selectedLanguage == "en") "Close ▲" else "إغلاق ▲") else (if (selectedLanguage == "en") "Set Reminder ＋" else "تحديد التذكير ＋"),
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
                                                lang = selectedLanguage,
                                                onSave = { degree, interval ->
                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                    viewModel.addRelative(
                                                        name = name,
                                                        phone = phone,
                                                        relationshipDegree = degree,
                                                        intervalDays = interval,
                                                        notes = if (selectedLanguage == "en") "Imported from contacts" else "تم استيراده من جهات الاتصال",
                                                        photoUri = photoUri
                                                    )
                                                    val toastMsg = if (selectedLanguage == "en") "$name successfully added to Silah! ✨" else "تمت إضافة $name بنجاح في صِلَةِ! ✨"
                                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
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
                        Text(if (selectedLanguage == "en") "Done & Return to List 🌸" else "تم والعودة للقائمة 🌸", fontWeight = FontWeight.Bold)
                    }
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
    lang: String = "ar",
    onSave: (degree: String, intervalDays: Int) -> Unit
) {
    var relationshipDegree by remember { mutableStateOf(viewModel.suggestRelationshipDegree(initialName)) }
    var intervalDays by remember { mutableIntStateOf(7) }

    val degrees = listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val degreeLabels = if (lang == "en") listOf("Parents", "Siblings", "Uncles/Aunts", "Other Relatives") else degrees

    val intervals = if (lang == "en") listOf(
        Pair(1, "Daily"),
        Pair(3, "Every 3 days"),
        Pair(7, "Weekly"),
        Pair(14, "Every 2 weeks"),
        Pair(30, "Monthly")
    ) else listOf(
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
        Text(if (lang == "en") "Relationship:" else "درجة القرابة:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(degrees.zip(degreeLabels)) { (degree, label) ->
                FilterChip(
                    selected = relationshipDegree == degree,
                    onClick = { relationshipDegree = degree },
                    label = { Text(label, fontSize = 10.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Text(if (lang == "en") "Reminder Interval:" else "موعد التذكير الدوري:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Text(if (lang == "en") "Save & Enable Reminder ✨" else "حفظ وتفعيل التذكير ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.CallLogManager
import com.example.data.Relative
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel

/**
 * AddEditRelativeDialog:
 * - Edit Mode: Modifies existing relative data.
 * - Add Mode: Allows selecting directly from Phone Contacts with quick reminder frequency setup,
 *   or switching to manual entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRelativeDialog(
    viewModel: RelativeViewModel,
    relativeToEdit: Relative? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isEditMode = relativeToEdit != null
    val lang by viewModel.selectedLanguage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = From Contacts, 1 = Manual Entry

    // Manual / Edit mode state
    var name by remember { mutableStateOf(relativeToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(relativeToEdit?.phone ?: "") }
    var relationshipDegree by remember {
        mutableStateOf(
            relativeToEdit?.relationshipDegree
                ?: viewModel.suggestRelationshipDegree(relativeToEdit?.name ?: "")
        )
    }
    var intervalDays by remember { mutableIntStateOf(relativeToEdit?.contactIntervalDays ?: 7) }
    var notes by remember { mutableStateOf(relativeToEdit?.notes ?: "") }

    // Contacts list state for Add mode
    val contacts by viewModel.deviceContacts.collectAsState()
    val isLoadingContacts by viewModel.isLoadingContacts.collectAsState()
    val existingRelatives by viewModel.relatives.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedContactPhone by remember { mutableStateOf<String?>(null) }

    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
        if (isGranted) {
            viewModel.fetchDeviceContacts(context)
        } else {
            Toast.makeText(
                context,
                if (lang == "en") "Permission needed to load contacts" else "يرجى منح الصلاحية لقراءة جهات الاتصال",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditMode && hasContactsPermission) {
            viewModel.fetchDeviceContacts(context)
        }
    }

    val existingNormalizedPhones = remember(existingRelatives) {
        existingRelatives.map { CallLogManager.normalizePhoneNumber(it.phone) }.toSet()
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { (cName, cPhone, _) ->
            cName.contains(searchQuery, ignoreCase = true) || cPhone.contains(searchQuery)
        }
    }

    val degrees = listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val degreeLabels = if (lang == "en")
        listOf("Parents", "Siblings", "Uncles/Aunts", "Other Relatives")
    else degrees

    val intervals = if (lang == "en") listOf(
        Pair(1, "Daily"),
        Pair(3, "Every 3 Days"),
        Pair(7, "Weekly"),
        Pair(14, "Every 2 Weeks"),
        Pair(30, "Monthly")
    ) else listOf(
        Pair(1, "يومياً"),
        Pair(3, "كل 3 أيام"),
        Pair(7, "كل أسبوع"),
        Pair(14, "كل أسبوعين"),
        Pair(30, "كل شهر")
    )

    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints {
            val isTablet = maxWidth > 600.dp
            val dialogWidth = if (isTablet) 560.dp else maxWidth

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .width(dialogWidth)
                    .fillMaxWidth()
                    .fillMaxHeight(if (isEditMode) 0.65f else 0.85f)
            ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Dialog Title
                Text(
                    text = if (isEditMode)
                        (if (lang == "en") "Edit ${relativeToEdit?.name} ✏️" else "تعديل بيانات ${relativeToEdit?.name} ✏️")
                    else
                        (if (lang == "en") "Add Relative to Silah 🌸" else "إضافة قريب لـ صِلَةِ 🌸"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!isEditMode) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Selector: Contacts vs Manual
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = if (lang == "en") "From Contacts 📲" else "من الجوال 📲",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = if (lang == "en") "Manual Entry ✍️" else "إدخال يدوي ✍️",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Body
                if (isEditMode || selectedTab == 1) {
                    // Manual Form (Edit or Manual Tab)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (!isEditMode) {
                                    relationshipDegree = viewModel.suggestRelationshipDegree(it)
                                }
                            },
                            label = { Text(if (lang == "en") "Relative's Name" else "اسم القريب") },
                            placeholder = { Text(if (lang == "en") "e.g. Mom, Uncle Ahmed" else "مثال: أمي الغالية، عاطف") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (lang == "en") "Phone Number" else "رقم الهاتف") },
                            placeholder = { Text("+201000000000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Text(if (lang == "en") "Relationship:" else "درجة القرابة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(degrees.zip(degreeLabels)) { (degree, label) ->
                                FilterChip(
                                    selected = relationshipDegree == degree,
                                    onClick = { relationshipDegree = degree },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Text(if (lang == "en") "Reminder Frequency:" else "معدل التذكير الدوري:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(intervals) { (days, label) ->
                                FilterChip(
                                    selected = intervalDays == days,
                                    onClick = { intervalDays = days },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(if (lang == "en") "Notes (optional)" else "ملاحظات (اختياري)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(if (lang == "en") "Cancel" else "إلغاء", color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        if (lang == "en") "Please enter a name and phone number"
                                        else "يرجى كتابة الاسم ورقم الهاتف على الأقل",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                if (isEditMode && relativeToEdit != null) {
                                    viewModel.editRelative(relativeToEdit, name, phone, relationshipDegree, intervalDays, notes)
                                    Toast.makeText(
                                        context,
                                        if (lang == "en") "$name updated successfully ✅" else "تم تحديث بيانات $name بنجاح ✅",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.addRelative(name, phone, relationshipDegree, intervalDays, notes)
                                    Toast.makeText(
                                        context,
                                        if (lang == "en") "$name added to Silah successfully! ✨" else "تمت إضافة $name بنجاح في صِلَةِ! ✨",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                if (isEditMode) (if (lang == "en") "Save Changes ✅" else "حفظ التعديلات ✅")
                                else (if (lang == "en") "Save & Activate Reminder ✨" else "حفظ وتفعيل التذكير ✨"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Contacts Selection Tab
                    if (!hasContactsPermission) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (lang == "en") "Permission required to access contacts" else "تطلب صِلَةِ صلاحية الوصول لجهات الاتصال لتسهيل اختيار أرحامك",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(if (lang == "en") "Grant Contacts Permission 📱" else "السماح بقراءة جهات الاتصال 📱", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(if (lang == "en") "Search contacts by name or phone..." else "ابحث عن قريب بالاسم أو رقم الهاتف...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoadingContacts) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(if (lang == "en") "Loading contacts..." else "جاري تحميل جهات الاتصال...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
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
                                    text = if (searchQuery.isNotEmpty())
                                        (if (lang == "en") "No contacts match search" else "لا توجد نتائج مطابقة لـ \"$searchQuery\"")
                                    else
                                        (if (lang == "en") "No contacts found on device" else "لم يتم العثور على جهات اتصال على الجهاز"),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredContacts) { (cName, cPhone, _) ->
                                    val normalized = CallLogManager.normalizePhoneNumber(cPhone)
                                    val isAlreadyAdded = existingNormalizedPhones.contains(normalized)
                                    val isExpanded = selectedContactPhone == cPhone

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isAlreadyAdded) {
                                                selectedContactPhone = if (isExpanded) null else cPhone
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isAlreadyAdded)
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            else if (isExpanded)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
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
                                                    Box(
                                                        contentAlignment = Alignment.Center,
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isAlreadyAdded) Color(0xFFE8F5E9)
                                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                            )
                                                    ) {
                                                        Text(
                                                            text = cName.take(1),
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isAlreadyAdded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                                            fontSize = 15.sp
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = cName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = if (isAlreadyAdded) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = cPhone,
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
                                                        Text(if (lang == "en") "Added 🟢" else "مضاف 🟢", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Text(
                                                        text = if (isExpanded) (if (lang == "en") "Close ▲" else "إغلاق ▲")
                                                               else (if (lang == "en") "Select Reminder ＋" else "تحديد التذكير ＋"),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Inline Setup Form when contact is tapped
                                            AnimatedVisibility(visible = isExpanded && !isAlreadyAdded) {
                                                InlineContactSetupForm(
                                                    initialName = cName,
                                                    viewModel = viewModel,
                                                    lang = lang,
                                                    degrees = degrees,
                                                    degreeLabels = degreeLabels,
                                                    intervals = intervals,
                                                    onSave = { degree, interval ->
                                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                        viewModel.addRelative(cName, cPhone, degree, interval, "تمت الإضافة من جهات الاتصال")
                                                        Toast.makeText(
                                                            context,
                                                            if (lang == "en") "$cName added to Silah successfully! ✨"
                                                            else "تمت إضافة $cName بنجاح في صِلَةِ! ✨",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        selectedContactPhone = null
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (lang == "en") "Done & Return 🌸" else "تم والعودة 🌸", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}

@Composable
private fun InlineContactSetupForm(
    initialName: String,
    viewModel: RelativeViewModel,
    lang: String,
    degrees: List<String>,
    degreeLabels: List<String>,
    intervals: List<Pair<Int, String>>,
    onSave: (degree: String, intervalDays: Int) -> Unit
) {
    var relationshipDegree by remember { mutableStateOf(viewModel.suggestRelationshipDegree(initialName)) }
    var intervalDays by remember { mutableIntStateOf(7) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(if (lang == "en") "Relationship Degree:" else "درجة القرابة:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        Text(if (lang == "en") "Reminder Frequency:" else "معدل التذكيرات الدوري (كل أد ايه؟):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Text(
                if (lang == "en") "Save & Activate Reminder ✨" else "حفظ وتفعيل التذكير ✨",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

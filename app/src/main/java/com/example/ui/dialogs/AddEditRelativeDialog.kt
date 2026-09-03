package com.example.ui.dialogs

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Edit
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

    // Pre-fill from Contact Picker if user picked a contact
    val pickedName by viewModel.pickedContactName.collectAsState()
    val pickedPhone by viewModel.pickedContactPhone.collectAsState()
    val hasPicked by viewModel.hasPendingPickedContact.collectAsState()

    // When a contact is picked, switch to manual tab with pre-filled data
    LaunchedEffect(hasPicked) {
        if (hasPicked && !isEditMode) {
            name = pickedName
            phone = pickedPhone
            if (pickedName.isNotBlank()) {
                relationshipDegree = viewModel.suggestRelationshipDegree(pickedName)
            }
            selectedTab = 1 // Switch to manual form so user can confirm/adjust
            viewModel.clearPickedContact()
        }
    }

    val existingRelatives by viewModel.relatives.collectAsState()
    val existingNormalizedPhones = remember(existingRelatives) {
        existingRelatives.map { it.phone.replace("[^\\d+]".toRegex(), "").takeLast(9) }.toSet()
    }




    val degrees = listOf("جد", "جدة", "أب", "أم", "أخ", "أخت", "عم", "عمة", "خال", "خالة", "أقارب آخرون")
    val degreeLabels = if (lang == "en")
        listOf("Grandfather", "Grandmother", "Father", "Mother", "Brother", "Sister", "Uncle (Pat.)", "Aunt (Pat.)", "Uncle (Mat.)", "Aunt (Mat.)", "Other Relatives")
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
            val isManualMode = isEditMode || selectedTab == 1

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .width(dialogWidth)
                    .fillMaxWidth()
                    .then(
                        if (isManualMode) Modifier.wrapContentHeight()
                        else Modifier.fillMaxHeight(0.82f)
                    )
            ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .then(if (isManualMode) Modifier.wrapContentHeight() else Modifier.fillMaxSize())
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

                Spacer(modifier = Modifier.height(14.dp))

                // Content Body
                if (isManualMode) {
                    // Manual Form (Edit or Manual Tab)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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
                    }
                } else {
                    // ── Contact Picker Tab (uses Android's native picker) ────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Big CTA button
                        Button(
                            onClick = { viewModel.launchContactPicker() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0E7075),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (lang == "en") "Choose from Contacts 📲" else "اختار من جهات الاتصال 📲",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Info card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("🔒", fontSize = 18.sp)
                                    Text(
                                        text = if (lang == "en")
                                            "Sila uses Android's built-in contact picker.\nOnly the name and phone number of the contact you select are stored — nothing is uploaded or shared."
                                        else
                                            "صِلَةِ تستخدم نافذة اختيار جهات الاتصال المدمجة في أندرويد.\nفقط اسم ورقم الشخص اللي تختاره يُحفظ على جهازك — لا يُرفع شيء ولا يُشارك.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Divider with OR
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = if (lang == "en") "  or  " else "  أو  ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        // Switch to Manual
                        OutlinedButton(
                            onClick = { selectedTab = 1 },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "en") "Enter manually instead ✍️" else "إدخال يدوي بدلاً منه ✍️",
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftGold,
                                contentColor = Color(0xFF141816)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (lang == "en") "Cancel" else "إلغاء", fontWeight = FontWeight.Bold)
                        }
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

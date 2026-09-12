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
import com.example.ui.components.ReminderIntervalSelector
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

    // Pre-fill from Contact Picker if user picked a contact
    val pickedName by viewModel.pickedContactName.collectAsState()
    val pickedPhone by viewModel.pickedContactPhone.collectAsState()
    val hasPicked by viewModel.hasPendingPickedContact.collectAsState()

    val initialPickedName = remember { viewModel.pickedContactName.value }
    val initialPickedPhone = remember { viewModel.pickedContactPhone.value }
    val initialHasPicked = remember { viewModel.hasPendingPickedContact.value }

    fun mapToDetailedDegree(rawName: String): String {
        val suggested = viewModel.suggestRelationshipDegree(rawName)
        val lower = rawName.lowercase()
        return when (suggested) {
            "والدان" -> if (lower.contains("أم") || lower.contains("امي") || lower.contains("ماما") || lower.contains("والدة") || lower.contains("والدتي")) "أم" else "أب"
            "أشقاء" -> if (lower.contains("أخت") || lower.contains("اخت") || lower.contains("شقيقة")) "أخت" else "أخ"
            "أعمام/أخوال" -> when {
                lower.contains("عمة") || lower.contains("عمتي") -> "عمة"
                lower.contains("خالة") || lower.contains("خالتي") -> "خالة"
                lower.contains("خال") -> "خال"
                else -> "عم"
            }
            else -> if (lower.contains("جدة")) "جدة" else if (lower.contains("جد")) "جد" else "أقارب آخرون"
        }
    }

    // Manual / Edit mode state
    var name by remember {
        mutableStateOf(relativeToEdit?.name ?: if (initialHasPicked) initialPickedName else "")
    }
    var phone by remember {
        mutableStateOf(relativeToEdit?.phone ?: if (initialHasPicked) initialPickedPhone else "")
    }
    var relationshipDegree by remember {
        mutableStateOf(
            relativeToEdit?.relationshipDegree
                ?: mapToDetailedDegree(relativeToEdit?.name ?: if (initialHasPicked) initialPickedName else "")
        )
    }
    var intervalDays by remember { mutableIntStateOf(relativeToEdit?.contactIntervalDays ?: 7) }
    var notes by remember { mutableStateOf(relativeToEdit?.notes ?: "") }

    // When a contact is picked, pre-fill form data directly
    LaunchedEffect(hasPicked) {
        if (hasPicked && !isEditMode) {
            if (pickedName.isNotBlank()) name = pickedName
            if (pickedPhone.isNotBlank()) phone = pickedPhone
            if (pickedName.isNotBlank()) {
                relationshipDegree = mapToDetailedDegree(pickedName)
            }
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

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .width(dialogWidth)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .wrapContentHeight()
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

                    // Choose from Contacts Action Button
                    Surface(
                        onClick = { viewModel.launchContactPicker() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "en") "Choose from Contacts 📲" else "اختر من جهات الاتصال 📲",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Form Content Body (Unified single form)
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
                        trailingIcon = {
                            if (!isEditMode) {
                                IconButton(onClick = { viewModel.launchContactPicker() }) {
                                    Icon(
                                        Icons.Default.ContactPhone,
                                        contentDescription = if (lang == "en") "Pick Contact" else "اختر من جهات الاتصال",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
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

                    ReminderIntervalSelector(
                        intervalDays = intervalDays,
                        onIntervalChange = { intervalDays = it },
                        relationshipDegree = relationshipDegree,
                        lang = lang
                    )

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
                                if (name.trim().isBlank()) {
                                    Toast.makeText(
                                        context,
                                        if (lang == "en") "Please enter the relative's name"
                                        else "يرجى كتابة اسم القريب على الأقل",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                if (relativeToEdit != null) {
                                    viewModel.editRelative(relativeToEdit, name.trim(), phone.trim(), relationshipDegree, intervalDays, notes.trim())
                                    Toast.makeText(
                                        context,
                                        if (lang == "en") "$name updated successfully ✅" else "تم تحديث بيانات $name بنجاح ✅",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.addRelative(name.trim(), phone.trim(), relationshipDegree, intervalDays, notes.trim())
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

        ReminderIntervalSelector(
            intervalDays = intervalDays,
            onIntervalChange = { intervalDays = it },
            relationshipDegree = relationshipDegree,
            lang = lang
        )

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

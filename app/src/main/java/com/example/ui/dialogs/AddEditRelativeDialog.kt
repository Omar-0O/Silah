package com.example.ui.dialogs

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Dialog واحد يعمل في وضعين:
 * - Add: لإضافة قريب جديد (relativeToEdit = null)
 * - Edit: لتعديل قريب موجود (relativeToEdit != null)
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

    // Degree labels — always stored as Arabic internally, displayed in the selected language
    val degrees = listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val degreeLabels = if (lang == "en")
        listOf("Parents", "Siblings", "Uncles/Aunts", "Other Relatives")
    else
        degrees

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
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Text(
                    text = if (isEditMode)
                        (if (lang == "en") "Edit ${relativeToEdit?.name} ✏️" else "تعديل بيانات ${relativeToEdit?.name} ✏️")
                    else
                        (if (lang == "en") "Add New Relative to Silah 🌸" else "إضافة قريب جديد لـ صِلَةِ 🌸"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Name Field
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

                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (lang == "en") "Phone Number" else "رقم الهاتف") },
                    placeholder = { Text("+966500000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // Relationship Degree
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

                // Reminder Interval
                Text(if (lang == "en") "Reminder Frequency:" else "معدل التذكيرات الدوري:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

                // Notes (optional)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (lang == "en") "Notes (optional)" else "ملاحظات (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 2
                )

                // Actions
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
                                    if (lang == "en") "$name updated successfully ✅"
                                    else "تم تحديث بيانات $name بنجاح ✅",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                viewModel.addRelative(name, phone, relationshipDegree, intervalDays, notes)
                                Toast.makeText(
                                    context,
                                    if (lang == "en") "$name added to Silah successfully! ✨"
                                    else "تمت إضافة $name بنجاح في صِلَةِ! ✨",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SoftGold,
                            contentColor = Color(0xFF141816)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isEditMode)
                                (if (lang == "en") "Save Changes ✅" else "حفظ التعديلات ✅")
                            else
                                (if (lang == "en") "Save & Activate Reminder ✨" else "حفظ وتفعيل التذكير ✨"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

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

    val degrees = listOf("والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون")
    val intervals = listOf(
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
                    text = if (isEditMode) "تعديل بيانات ${relativeToEdit?.name} ✏️"
                           else "إضافة قريب جديد لـ صِلَةِ 🌸",
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
                    label = { Text("اسم القريب") },
                    placeholder = { Text("مثال: أمي الغالية، عاطف") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    placeholder = { Text("+966500000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // Relationship Degree
                Text("درجة القرابة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(degrees) { degree ->
                        FilterChip(
                            selected = relationshipDegree == degree,
                            onClick = { relationshipDegree = degree },
                            label = { Text(degree, fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Reminder Interval
                Text("معدل التذكيرات الدوري:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    label = { Text("ملاحظات (اختياري)") },
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
                        Text("إلغاء", color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                Toast.makeText(context, "يرجى كتابة الاسم ورقم الهاتف على الأقل", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            if (isEditMode && relativeToEdit != null) {
                                viewModel.editRelative(relativeToEdit, name, phone, relationshipDegree, intervalDays, notes)
                                Toast.makeText(context, "تم تحديث بيانات $name بنجاح ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addRelative(name, phone, relationshipDegree, intervalDays, notes)
                                Toast.makeText(context, "تمت إضافة $name بنجاح في صِلَةِ! ✨", Toast.LENGTH_SHORT).show()
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
                            if (isEditMode) "حفظ التعديلات ✅" else "حفظ وتفعيل التذكير ✨",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

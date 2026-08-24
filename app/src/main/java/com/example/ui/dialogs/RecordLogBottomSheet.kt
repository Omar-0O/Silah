package com.example.ui.dialogs

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Relative
import com.example.ui.theme.SoftGold
import com.example.viewmodel.RelativeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordLogBottomSheet(
    relative: Relative,
    viewModel: RelativeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lang by viewModel.selectedLanguage.collectAsState()
    var selectedType by remember { mutableStateOf("مكالمة") }
    var notes by remember { mutableStateOf("") }

    val commTypes = if (lang == "en") listOf(
        Triple("مكالمة", "Phone Call 📞", Icons.Default.Call),
        Triple("زيارة", "Family Visit 🏠", Icons.Default.People),
        Triple("رسالة", "Message/WhatsApp ✉️", Icons.AutoMirrored.Filled.Chat),
        Triple("هدية", "Gift/Occasion 🎁", Icons.Default.Star)
    ) else listOf(
        Triple("مكالمة", "مكالمة هاتفية 📞", Icons.Default.Call),
        Triple("زيارة", "زيارة عائلية 🏠", Icons.Default.People),
        Triple("رسالة", "رسالة نصية/واتساب ✉️", Icons.AutoMirrored.Filled.Chat),
        Triple("هدية", "هدية/مناسبة 🎁", Icons.Default.Star)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (lang == "en") "Log Communication with ${relative.name} 🌸"
                       else "تسجيل تواصل مع ${relative.name} 🌸",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (lang == "en") "Choose the type of contact and record it to keep your journey alive 💚"
                       else "اختر طريقة التواصل وسجّلها لتبقى في ميزان حسناتك ومسيرتك 💚",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(commTypes) { (type, label, icon) ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(if (lang == "en") "Notes or impressions about this contact (optional)" else "ملاحظات أو انطباعات عن التواصل (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.recordCommunication(
                        relative.id, selectedType,
                        notes.ifEmpty { if (lang == "en") "A blessed connection" else "تواصل طيب ومبارك" }
                    )
                    Toast.makeText(
                        context,
                        if (lang == "en") "Contact logged successfully! ✨" else "تم حفظ تسجيل التواصل بنجاح! ✨",
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    if (lang == "en") "Save & Confirm Contact 🌸" else "حفظ وتأكيد التواصل 🌸",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

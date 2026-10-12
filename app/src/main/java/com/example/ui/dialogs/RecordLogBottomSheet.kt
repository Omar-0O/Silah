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

    val commTypes = listOf(
        Triple("مكالمة", androidx.compose.ui.res.stringResource(com.example.R.string.comm_type_call), Icons.Default.Call),
        Triple("زيارة", androidx.compose.ui.res.stringResource(com.example.R.string.comm_type_visit), Icons.Default.People),
        Triple("رسالة", androidx.compose.ui.res.stringResource(com.example.R.string.comm_type_message), Icons.AutoMirrored.Filled.Chat),
        Triple("هدية", androidx.compose.ui.res.stringResource(com.example.R.string.comm_type_gift), Icons.Default.Star)
    )

    val defaultNote = androidx.compose.ui.res.stringResource(com.example.R.string.comm_default_blessed)
    val successMsg = androidx.compose.ui.res.stringResource(com.example.R.string.comm_logged_success)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.R.string.log_comm_title, relative.name),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = androidx.compose.ui.res.stringResource(com.example.R.string.log_comm_desc),
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
                label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.comm_notes_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.recordCommunication(
                        relative.id, selectedType,
                        notes.ifEmpty { defaultNote }
                    )
                    Toast.makeText(
                        context,
                        successMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color(0xFF141816)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    androidx.compose.ui.res.stringResource(com.example.R.string.save_confirm_comm),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

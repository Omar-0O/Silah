package com.example.ui.components

import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryGreen
import java.io.File

/**
 * Bottom sheet for managing the user's personal profile photo.
 * Lets the user upload a picture from their device gallery, remove their existing photo,
 * or cancel. Zero preset planet avatars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoSheet(
    photoPath: String?,
    userName: String = "",
    timestamp: Long = 0L,
    lang: String = "ar",
    onPhotoSelected: (Uri) -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val hasPhoto = !photoPath.isNullOrBlank() && File(photoPath).let { it.exists() && it.length() > 0 }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                android.util.Log.d("SilaPhoto", "ProfilePhotoSheet onResult: $uri")
                onPhotoSelected(uri)
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                Toast.makeText(
                    context,
                    if (lang == "en") "Profile photo updated! 📸" else "تم تحديث الصورة الشخصية بنجاح 📸",
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
            } catch (e: Exception) {
                android.util.Log.e("SilaPhoto", "Error in ProfilePhotoSheet", e)
                Toast.makeText(
                    context,
                    if (lang == "en") "Failed to load image" else "تعذر تحميل الصورة، يرجى المحاولة مرة أخرى",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Title
            Text(
                text = if (lang == "en") "Profile Photo" else "الصورة الشخصية",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (lang == "en") "Upload a personal picture or leave it blank with your initials"
                else "يمكنك رفع صورة شخصية لك أو تركها بحرف اسمك",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Avatar Preview
            SilaUserAvatar(
                photoPath = photoPath,
                userName = userName,
                size = 96.dp,
                showBorder = true,
                timestamp = timestamp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Option 1: Choose Photo from Gallery
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryGreen.copy(alpha = 0.09f)
                ),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        photoPickerLauncher.launch("image/*")
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "Choose from Gallery" else "اختيار صورة من المعرض",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == "en") "Pick a photo from your device" else "اختر صورة شخصية من جهازك",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Option 2: Remove Photo (Only if photo is set)
            if (hasPhoto) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AlertRed.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onRemovePhoto()
                            Toast.makeText(
                                context,
                                if (lang == "en") "Profile photo removed" else "تمت إزالة الصورة الشخصية",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AlertRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "en") "Remove Photo" else "إزالة الصورة الحالية",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                            Text(
                                text = if (lang == "en") "Revert to name monogram" else "العودة للرمز التعبيري بالحرف",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cancel Button
            TextButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (lang == "en") "Cancel" else "إلغاء",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

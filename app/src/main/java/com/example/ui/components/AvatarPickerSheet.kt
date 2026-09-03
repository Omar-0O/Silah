package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoftGold

/**
 * Full-screen bottom sheet for picking a profile avatar.
 * Shows a 4-column emoji grid; tapping an avatar selects it and saves via [onAvatarSelected].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    currentAvatarId: String,
    lang: String = "ar",
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedId by remember { mutableStateOf(currentAvatarId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            Text(
                text = if (lang == "en") "Choose Your Avatar" else "اختر صورتك الشخصية",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = if (lang == "en") "Tap an avatar to preview, then tap Save"
                       else "اضغط على الصورة للمعاينة، ثم اضغط حفظ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            var genderFilter by remember { mutableStateOf("all") } // "all", "male", "female"

            val filteredAvatars = remember(genderFilter) {
                when (genderFilter) {
                    "male" -> ALL_AVATARS.filter { it.gender == "male" }
                    "female" -> ALL_AVATARS.filter { it.gender == "female" }
                    else -> ALL_AVATARS
                }
            }

            // ── Gender Filter Chips ──────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                FilterChip(
                    selected = genderFilter == "all",
                    onClick = { genderFilter = "all" },
                    label = { Text(if (lang == "en") "All ✨" else "الكل ✨", fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = genderFilter == "male",
                    onClick = { genderFilter = "male" },
                    label = { Text(if (lang == "en") "Male 👨" else "ذكور 👨", fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = genderFilter == "female",
                    onClick = { genderFilter = "female" },
                    label = { Text(if (lang == "en") "Female 👩" else "إناث 👩", fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Avatar Grid ──────────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                items(filteredAvatars) { avatar ->
                    AvatarGridItem(
                        avatar = avatar,
                        isSelected = avatar.id == selectedId,
                        lang = lang,
                        onClick = { selectedId = avatar.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Save Button ──────────────────────────────────────────────────
            Button(
                onClick = {
                    onAvatarSelected(selectedId)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftGold,
                    contentColor = Color(0xFF141816)
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == "en") "Save Avatar" else "حفظ الصورة",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AvatarGridItem(
    avatar: AvatarDef,
    isSelected: Boolean,
    lang: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(62.dp)
        ) {
            SilaUserAvatar(
                avatarId = avatar.id,
                size = 60.dp,
                showBorder = isSelected
            )

            // Selected checkmark overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0x55000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = SoftGold,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Text(
            text = if (lang == "en") avatar.labelEn else avatar.label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

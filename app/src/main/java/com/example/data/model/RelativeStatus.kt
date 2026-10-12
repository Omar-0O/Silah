package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
enum class RelativeStatus(
    val label: String,
    val labelEn: String,
    val colorHex: String,
    val color: Color
) {
    NEEDS_CONTACT_URGENT("لم تتواصل معه بعد.. ابدأ اليوم 🌿", "Contact Now (Never Called)", "E53935", Color(0xFFE53935)),
    OVERDUE_CRITICAL("شخص عزيز مشتاق لسماع صوتك! ❤️", "Very Overdue!", "D32F2F", Color(0xFFD32F2F)),
    NEEDS_CONTACT("اليوم فرصة جميلة لتطمئن عليه 🌸", "Time to Connect Today", "EF6C00", Color(0xFFEF6C00)),
    OK_SOON("طمئنه قريبًا 🌿", "Connect Soon", "FBC02D", Color(0xFFFBC02D)),
    CONNECTED("تواصل مبارك ومستمر ✨", "Great! Recently Connected", "2E7D32", Color(0xFF2E7D32));

    fun getLabel(lang: String) = if (lang == "en") labelEn else label
}

package com.example.ui.components

import androidx.compose.ui.graphics.Color

/**
 * A single selectable avatar: emoji displayed inside a coloured circle.
 */
data class AvatarDef(
    val id: String,
    val emoji: String,
    val bgFrom: Color,
    val bgTo: Color,
    val label: String,       // Arabic
    val labelEn: String      // English
)

val ALL_AVATARS = listOf(
    // ── Family & People ──────────────────────────────────────────────────────
    AvatarDef("avatar_01", "👨", Color(0xFF0E4D7A), Color(0xFF1A78C2), "رجل", "Man"),
    AvatarDef("avatar_02", "👩", Color(0xFF7A0E4D), Color(0xFFC21A78), "امرأة", "Woman"),
    AvatarDef("avatar_03", "👴", Color(0xFF4D3B0E), Color(0xFFA07830), "رجل كبير", "Elder Man"),
    AvatarDef("avatar_04", "👵", Color(0xFF5C3A1E), Color(0xFF9E6B42), "امرأة كبيرة", "Elder Woman"),
    AvatarDef("avatar_05", "🧑", Color(0xFF0E5C2E), Color(0xFF1A9E52), "شاب", "Youth"),
    AvatarDef("avatar_06", "👦", Color(0xFF0E4D4D), Color(0xFF1A9D9D), "فتى", "Boy"),
    AvatarDef("avatar_07", "👧", Color(0xFF5C0E4D), Color(0xFF9D1A8B), "فتاة", "Girl"),
    AvatarDef("avatar_08", "🧔", Color(0xFF2E1A0E), Color(0xFF7A4A1A), "رجل ذو لحية", "Bearded Man"),
    AvatarDef("avatar_09", "🧕", Color(0xFF1A0E5C), Color(0xFF3A1A9D), "محجبة", "Hijabi"),
    AvatarDef("avatar_10", "👨‍🦳", Color(0xFF1A3D5C), Color(0xFF2A6A9D), "رجل شعر أبيض", "Silver Hair"),
    AvatarDef("avatar_11", "👩‍🦳", Color(0xFF5C1A3D), Color(0xFF9D2A6A), "امرأة شعر أبيض", "Silver Hair Lady"),
    AvatarDef("avatar_12", "🧒", Color(0xFF3D5C1A), Color(0xFF6A9D2A), "طفل", "Child"),

    // ── Professions & Roles ──────────────────────────────────────────────────
    AvatarDef("avatar_13", "👨‍⚕️", Color(0xFF0E5C4D), Color(0xFF1A9D85), "طبيب", "Doctor"),
    AvatarDef("avatar_14", "👩‍⚕️", Color(0xFF5C0E1A), Color(0xFF9D1A30), "طبيبة", "Doctor Lady"),
    AvatarDef("avatar_15", "👨‍🏫", Color(0xFF3D0E5C), Color(0xFF6A1A9D), "معلم", "Teacher"),
    AvatarDef("avatar_16", "👩‍🏫", Color(0xFF5C3D0E), Color(0xFF9D6A1A), "معلمة", "Teacher Lady"),
    AvatarDef("avatar_17", "👨‍💼", Color(0xFF0E2D5C), Color(0xFF1A4E9D), "موظف", "Professional"),
    AvatarDef("avatar_18", "👩‍💼", Color(0xFF5C0E2D), Color(0xFF9D1A4E), "موظفة", "Professional Lady"),

    // ── Fun & Expressive ─────────────────────────────────────────────────────
    AvatarDef("avatar_19", "🤴", Color(0xFF5C4A00), Color(0xFFA08800), "أمير", "Prince"),
    AvatarDef("avatar_20", "👸", Color(0xFF5C0030), Color(0xFFA00055), "أميرة", "Princess"),
    AvatarDef("avatar_21", "🦸", Color(0xFF1A0E5C), Color(0xFF3A1A9D), "بطل", "Hero"),
    AvatarDef("avatar_22", "🦸‍♀️", Color(0xFF5C0E1A), Color(0xFF9D1A30), "بطلة", "Heroine"),
    AvatarDef("avatar_23", "🧙", Color(0xFF1A400E), Color(0xFF2E7A18), "حكيم", "Sage"),
    AvatarDef("avatar_24", "🧑‍🚀", Color(0xFF001A40), Color(0xFF003380), "رائد فضاء", "Astronaut"),
)

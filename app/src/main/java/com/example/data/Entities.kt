package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relatives")
data class Relative(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val relationshipDegree: String, // e.g., "والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون"
    val lastContactDate: Long = 0, // Timestamp in milliseconds
    val contactIntervalDays: Int = 14, // Reminder every X days
    val notes: String = ""
)

@Entity(tableName = "communication_logs")
data class CommunicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val relativeId: Int,
    val type: String, // e.g., "اتصال", "رسالة", "زيارة"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "quick_templates")
data class QuickTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String // e.g., "تهنئة", "يوم الجمعة", "مناسبات"
)

@Entity(tableName = "family_memories")
data class FamilyMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val relativeId: Int,
    val relativeName: String,
    val title: String,
    val description: String,
    val imagePath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

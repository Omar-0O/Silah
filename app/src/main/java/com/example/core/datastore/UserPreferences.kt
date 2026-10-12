package com.example.core.datastore

import androidx.compose.runtime.Immutable

/**
 * Immutable data class representing the user's stored preferences.
 */
@Immutable
data class UserPreferences(
    val isDarkMode: Boolean = false,
    val selectedLanguage: String = "ar",
    val userName: String = "",
    val userGender: String = "male",
    val userAvatarId: String = "avatar_01",
    val userPhotoPath: String? = null,
    val userPhotoTimestamp: Long = 0L,
    val reminderHour: Int = 10,
    val reminderMinute: Int = 0,
    val prefNotifyDueRelatives: Boolean = true,
    val prefNotifyEncouragement: Boolean = true,
    val prefNotifyMonthly: Boolean = true,
    val onboardingDone: Boolean = false,
    val appFirstLaunchTime: Long = 0L,
    val achievedMilestones: Set<String> = emptySet(),
    val lastSupportPromptTime: Long = 0L
)

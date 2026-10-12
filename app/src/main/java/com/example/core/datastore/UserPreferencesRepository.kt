package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(context, "app_settings")
        )
    }
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.userDataStore
    private val legacyPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_GENDER = stringPreferencesKey("user_gender")
        val KEY_USER_AVATAR_ID = stringPreferencesKey("user_avatar_id")
        val KEY_USER_PHOTO_PATH = stringPreferencesKey("user_photo_path")
        val KEY_USER_PHOTO_TS = longPreferencesKey("user_photo_ts")
        val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val KEY_PREF_NOTIFY_DUE_RELATIVES = booleanPreferencesKey("pref_notify_due_relatives")
        val KEY_PREF_NOTIFY_ENCOURAGEMENT = booleanPreferencesKey("pref_notify_encouragement")
        val KEY_PREF_NOTIFY_MONTHLY = booleanPreferencesKey("pref_notify_monthly")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_APP_FIRST_LAUNCH_TIME = longPreferencesKey("app_first_launch_time")
        val KEY_ACHIEVED_MILESTONES = stringSetPreferencesKey("achieved_milestones")
        val KEY_LAST_SUPPORT_PROMPT_TIME = longPreferencesKey("last_support_prompt_time")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapToUserPreferences(preferences)
        }

    private fun mapToUserPreferences(preferences: Preferences): UserPreferences {
        return UserPreferences(
            isDarkMode = preferences[KEY_DARK_MODE] ?: legacyPrefs.getBoolean("dark_mode", false),
            selectedLanguage = preferences[KEY_SELECTED_LANGUAGE] ?: legacyPrefs.getString("selected_language", "ar") ?: "ar",
            userName = preferences[KEY_USER_NAME] ?: legacyPrefs.getString("user_name", "") ?: "",
            userGender = preferences[KEY_USER_GENDER] ?: legacyPrefs.getString("user_gender", "male") ?: "male",
            userAvatarId = preferences[KEY_USER_AVATAR_ID] ?: legacyPrefs.getString("user_avatar_id", "avatar_01") ?: "avatar_01",
            userPhotoPath = preferences[KEY_USER_PHOTO_PATH] ?: legacyPrefs.getString("user_photo_path", null),
            userPhotoTimestamp = preferences[KEY_USER_PHOTO_TS] ?: legacyPrefs.getLong("user_photo_ts", 0L),
            reminderHour = preferences[KEY_REMINDER_HOUR] ?: legacyPrefs.getInt("reminder_hour", 10),
            reminderMinute = preferences[KEY_REMINDER_MINUTE] ?: legacyPrefs.getInt("reminder_minute", 0),
            prefNotifyDueRelatives = preferences[KEY_PREF_NOTIFY_DUE_RELATIVES] ?: legacyPrefs.getBoolean("pref_notify_due_relatives", true),
            prefNotifyEncouragement = preferences[KEY_PREF_NOTIFY_ENCOURAGEMENT] ?: legacyPrefs.getBoolean("pref_notify_encouragement", true),
            prefNotifyMonthly = preferences[KEY_PREF_NOTIFY_MONTHLY] ?: legacyPrefs.getBoolean("pref_notify_monthly", true),
            onboardingDone = preferences[KEY_ONBOARDING_DONE] ?: legacyPrefs.getBoolean("onboarding_done", false),
            appFirstLaunchTime = preferences[KEY_APP_FIRST_LAUNCH_TIME] ?: legacyPrefs.getLong("app_first_launch_time", 0L),
            achievedMilestones = preferences[KEY_ACHIEVED_MILESTONES] ?: (legacyPrefs.getStringSet("achieved_milestones", emptySet()) ?: emptySet()),
            lastSupportPromptTime = preferences[KEY_LAST_SUPPORT_PROMPT_TIME] ?: legacyPrefs.getLong("last_support_prompt_time", 0L)
        )
    }

    suspend fun getInitialPreferences(): UserPreferences {
        return try {
            val prefs = dataStore.data.first()
            mapToUserPreferences(prefs)
        } catch (e: Exception) {
            UserPreferences()
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
        legacyPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    suspend fun setSelectedLanguage(langCode: String) {
        dataStore.edit { it[KEY_SELECTED_LANGUAGE] = langCode }
        legacyPrefs.edit().putString("selected_language", langCode).apply()
    }

    suspend fun setUserProfile(name: String, gender: String) {
        dataStore.edit {
            it[KEY_USER_NAME] = name
            it[KEY_USER_GENDER] = gender
        }
        legacyPrefs.edit().putString("user_name", name).putString("user_gender", gender).apply()
    }

    suspend fun setUserAvatarId(avatarId: String) {
        dataStore.edit { it[KEY_USER_AVATAR_ID] = avatarId }
        legacyPrefs.edit().putString("user_avatar_id", avatarId).apply()
    }

    suspend fun setUserPhoto(path: String?, timestamp: Long) {
        dataStore.edit {
            if (path != null) {
                it[KEY_USER_PHOTO_PATH] = path
            } else {
                it.remove(KEY_USER_PHOTO_PATH)
            }
            it[KEY_USER_PHOTO_TS] = timestamp
        }
        if (path != null) {
            legacyPrefs.edit().putString("user_photo_path", path).putLong("user_photo_ts", timestamp).apply()
        } else {
            legacyPrefs.edit().remove("user_photo_path").putLong("user_photo_ts", timestamp).apply()
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[KEY_REMINDER_HOUR] = hour
            it[KEY_REMINDER_MINUTE] = minute
        }
        legacyPrefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
    }

    suspend fun setPrefNotifyDueRelatives(enabled: Boolean) {
        dataStore.edit { it[KEY_PREF_NOTIFY_DUE_RELATIVES] = enabled }
        legacyPrefs.edit().putBoolean("pref_notify_due_relatives", enabled).apply()
    }

    suspend fun setPrefNotifyEncouragement(enabled: Boolean) {
        dataStore.edit { it[KEY_PREF_NOTIFY_ENCOURAGEMENT] = enabled }
        legacyPrefs.edit().putBoolean("pref_notify_encouragement", enabled).apply()
    }

    suspend fun setPrefNotifyMonthly(enabled: Boolean) {
        dataStore.edit { it[KEY_PREF_NOTIFY_MONTHLY] = enabled }
        legacyPrefs.edit().putBoolean("pref_notify_monthly", enabled).apply()
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
        legacyPrefs.edit().putBoolean("onboarding_done", done).apply()
    }

    suspend fun setAppFirstLaunchTime(time: Long) {
        dataStore.edit { it[KEY_APP_FIRST_LAUNCH_TIME] = time }
        legacyPrefs.edit().putLong("app_first_launch_time", time).apply()
    }

    suspend fun addAchievedMilestone(milestoneId: String) {
        dataStore.edit {
            val current = it[KEY_ACHIEVED_MILESTONES] ?: emptySet()
            it[KEY_ACHIEVED_MILESTONES] = current + milestoneId
        }
        val currentSet = legacyPrefs.getStringSet("achieved_milestones", emptySet()) ?: emptySet()
        legacyPrefs.edit().putStringSet("achieved_milestones", currentSet + milestoneId).apply()
    }

    suspend fun setLastSupportPromptTime(time: Long) {
        dataStore.edit { it[KEY_LAST_SUPPORT_PROMPT_TIME] = time }
        legacyPrefs.edit().putLong("last_support_prompt_time", time).apply()
    }
}

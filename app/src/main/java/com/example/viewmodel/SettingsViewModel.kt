package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.datastore.UserPreferencesRepository
import com.example.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private fun safeGetString(key: String, default: String): String {
        return try { prefs.getString(key, default) ?: default } catch (e: Exception) { default }
    }
    private fun safeGetBoolean(key: String, default: Boolean): Boolean {
        return try { prefs.getBoolean(key, default) } catch (e: Exception) { default }
    }
    private fun safeGetInt(key: String, default: Int): Int {
        return try { prefs.getInt(key, default) } catch (e: Exception) { default }
    }

    val isDarkMode = MutableStateFlow(safeGetBoolean("dark_mode", false))
    val selectedLanguage = MutableStateFlow(safeGetString("selected_language", "ar"))

    val prefNotifyDueRelatives = MutableStateFlow(safeGetBoolean("pref_notify_due_relatives", true))
    val prefNotifyEncouragement = MutableStateFlow(safeGetBoolean("pref_notify_encouragement", true))
    val prefNotifyMonthly = MutableStateFlow(safeGetBoolean("pref_notify_monthly", true))

    val reminderHour = MutableStateFlow(safeGetInt("reminder_hour", ReminderScheduler.DEFAULT_REMINDER_HOUR))
    val reminderMinute = MutableStateFlow(safeGetInt("reminder_minute", ReminderScheduler.DEFAULT_REMINDER_MINUTE))

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { p ->
                isDarkMode.value = p.isDarkMode
                selectedLanguage.value = p.selectedLanguage
                prefNotifyDueRelatives.value = p.prefNotifyDueRelatives
                prefNotifyEncouragement.value = p.prefNotifyEncouragement
                prefNotifyMonthly.value = p.prefNotifyMonthly
                reminderHour.value = p.reminderHour
                reminderMinute.value = p.reminderMinute
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        viewModelScope.launch { userPreferencesRepository.setDarkMode(enabled) }
    }

    fun selectLanguage(langCode: String) {
        selectedLanguage.value = langCode
        prefs.edit().putString("selected_language", langCode).apply()
        viewModelScope.launch { userPreferencesRepository.setSelectedLanguage(langCode) }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        reminderHour.value = hour
        reminderMinute.value = minute
        prefs.edit()
            .putInt("reminder_hour", hour)
            .putInt("reminder_minute", minute)
            .apply()
        viewModelScope.launch { userPreferencesRepository.setReminderTime(hour, minute) }
        ReminderScheduler.scheduleDailyReminder(getApplication())
    }

    fun toggleNotifyDueRelatives(enabled: Boolean) {
        prefNotifyDueRelatives.value = enabled
        prefs.edit().putBoolean("pref_notify_due_relatives", enabled).apply()
        viewModelScope.launch { userPreferencesRepository.setPrefNotifyDueRelatives(enabled) }
        if (enabled) {
            ReminderScheduler.scheduleDailyReminder(getApplication())
        } else {
            ReminderScheduler.cancelDailyReminder(getApplication())
        }
    }

    fun toggleNotifyEncouragement(enabled: Boolean) {
        prefNotifyEncouragement.value = enabled
        prefs.edit().putBoolean("pref_notify_encouragement", enabled).apply()
        viewModelScope.launch { userPreferencesRepository.setPrefNotifyEncouragement(enabled) }
    }

    fun toggleNotifyMonthly(enabled: Boolean) {
        prefNotifyMonthly.value = enabled
        prefs.edit().putBoolean("pref_notify_monthly", enabled).apply()
        viewModelScope.launch { userPreferencesRepository.setPrefNotifyMonthly(enabled) }
    }
}

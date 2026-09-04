package com.example.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.AlarmManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Central scheduler for kinship reminder notifications.
 * Combines AlarmManager (primary, wakes up device at exact/idle time even when app is killed)
 * with WorkManager (secondary, fallback periodic check).
 */
object ReminderScheduler {

    const val ACTION_DAILY_REMINDER = "com.example.silatrahim.ACTION_DAILY_REMINDER"
    const val REQUEST_CODE_DAILY_REMINDER = 5001

    private const val PREFS_NAME = "app_settings"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    private const val KEY_NOTIFY_DUE = "pref_notify_due_relatives"
    private const val NOTIFIED_PREFIX = "notified_relatives_"

    const val DEFAULT_REMINDER_HOUR = 10
    const val DEFAULT_REMINDER_MINUTE = 0

    /**
     * Calculates the next epoch millis for the given hour and minute.
     * If the time for today has already passed, schedules for tomorrow at the same time.
     */
    fun calculateNextTriggerTime(
        hour: Int = DEFAULT_REMINDER_HOUR,
        minute: Int = DEFAULT_REMINDER_MINUTE,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If scheduled time has already passed today, advance to tomorrow
        if (calendar.timeInMillis <= currentTimeMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    /**
     * Schedules the daily reminder alarm with AlarmManager.
     * Uses setExactAndAllowWhileIdle when permitted, falling back gracefully to setAndAllowWhileIdle.
     */
    fun scheduleDailyReminder(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_NOTIFY_DUE, true)
        if (!isEnabled) {
            cancelDailyReminder(context)
            return
        }

        val hour = prefs.getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR)
        val minute = prefs.getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE)

        val triggerTime = calculateNextTriggerTime(hour, minute)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = getAlarmPendingIntent(context)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    AlarmManagerCompat.setExactAndAllowWhileIdle(
                        alarmManager,
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    AlarmManagerCompat.setAndAllowWhileIdle(
                        alarmManager,
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback if exact alarm permission was revoked
            try {
                AlarmManagerCompat.setAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Also ensure periodic work fallback is scheduled
        schedulePeriodicWorkFallback(context)
    }

    /**
     * Cancels the scheduled daily reminder alarm.
     */
    fun cancelDailyReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val pendingIntent = getAlarmPendingIntent(context)
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAlarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Schedules a WorkManager periodic task as a secondary safety net.
     */
    fun schedulePeriodicWorkFallback(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            val fallbackWork = PeriodicWorkRequestBuilder<PeriodicDueWorker>(4, TimeUnit.HOURS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "sila_periodic_due_check",
                ExistingPeriodicWorkPolicy.KEEP,
                fallbackWork
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    const val KEY_PENDING_CONFIRMATION = "pending_confirmation_relatives"

    /**
     * Returns true if a notification has already been sent today for the given relative ID.
     * Prevents duplicate spamming when alarms or background workers fire multiple times.
     */
    fun wasNotifiedToday(context: Context, relativeId: Int, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = getTodayKey(currentTimeMillis)
        val sentSet = prefs.getStringSet(todayKey, emptySet()) ?: emptySet()
        return sentSet.contains(relativeId.toString())
    }

    /**
     * Marks that a notification was sent today for the given relative ID.
     * Also records the relative in the pending check-in confirmation set.
     */
    fun markNotifiedToday(context: Context, relativeId: Int, currentTimeMillis: Long = System.currentTimeMillis()) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayKey = getTodayKey(currentTimeMillis)
        val currentSet = prefs.getStringSet(todayKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(relativeId.toString())
        prefs.edit().putStringSet(todayKey, currentSet).apply()

        // Also add to pending confirmation set for in-app check-in popup
        addPendingNotifiedRelative(context, relativeId)

        // Clean up older keys to keep preferences lean
        cleanOldNotifiedKeys(prefs, todayKey)
    }

    fun addPendingNotifiedRelative(context: Context, relativeId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PENDING_CONFIRMATION, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(relativeId.toString())
        prefs.edit().putStringSet(KEY_PENDING_CONFIRMATION, current).apply()
    }

    fun removePendingNotifiedRelative(context: Context, relativeId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PENDING_CONFIRMATION, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (current.remove(relativeId.toString())) {
            prefs.edit().putStringSet(KEY_PENDING_CONFIRMATION, current).apply()
        }
    }

    fun getPendingNotifiedRelativeIds(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_PENDING_CONFIRMATION, emptySet()) ?: emptySet()
        return current.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun clearAllPendingNotifiedRelatives(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PENDING_CONFIRMATION).apply()
    }

    fun getTodayKey(currentTimeMillis: Long = System.currentTimeMillis()): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return NOTIFIED_PREFIX + dateFormat.format(Date(currentTimeMillis))
    }

    private fun cleanOldNotifiedKeys(prefs: android.content.SharedPreferences, currentKey: String) {
        try {
            val keysToRemove = prefs.all.keys.filter { it.startsWith(NOTIFIED_PREFIX) && it != currentKey }
            if (keysToRemove.isNotEmpty()) {
                val editor = prefs.edit()
                keysToRemove.forEach { editor.remove(it) }
                editor.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Checks whether the app is whitelisted from battery optimizations.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }
    }

    /**
     * Creates an intent to guide or request ignoring battery optimizations.
     */
    fun createIgnoreBatteryOptimizationIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            } catch (e: Exception) {
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            }
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
    }
}

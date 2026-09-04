package com.example.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.widget.SilaAppWidgetProvider

/**
 * BroadcastReceiver triggered when the device boots or app package is updated.
 * Automatically restores AlarmManager alarms and WorkManager periodic checks so reminders
 * continue working seamlessly after a phone reboot without requiring the user to open the app.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            try {
                // Restore daily reminder alarm
                ReminderScheduler.scheduleDailyReminder(context)

                // Restore periodic fallback work
                ReminderScheduler.schedulePeriodicWorkFallback(context)

                // Refresh widgets on boot
                SilaAppWidgetProvider.triggerWidgetUpdate(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

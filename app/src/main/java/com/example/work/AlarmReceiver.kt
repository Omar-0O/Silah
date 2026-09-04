package com.example.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager even when the app is completely closed or device is in Doze mode.
 * Performs kinship due check in background and shows degree-aware notifications without opening the app.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val notifyDue = prefs.getBoolean("pref_notify_due_relatives", true)

                if (notifyDue) {
                    val db = AppDatabase.getDatabase(context)
                    val relatives = db.relativeDao().getAllRelativesOnce()

                    val userName = prefs.getString("user_name", "") ?: ""
                    val lang = prefs.getString("selected_language", "ar") ?: "ar"
                    val now = System.currentTimeMillis()

                    for (relative in relatives) {
                        val lastContact = relative.lastContactDate
                        val intervalDays = relative.contactIntervalDays

                        val isDue = if (lastContact == 0L) {
                            true // Never contacted -> due immediately
                        } else {
                            val diffMs = now - lastContact
                            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                            diffDays >= intervalDays
                        }

                        if (isDue && !ReminderScheduler.wasNotifiedToday(context, relative.id, now)) {
                            SilaNotificationHelper.sendKinshipNotification(
                                context = context,
                                relative = relative,
                                lang = lang,
                                userName = userName
                            )

                            // Prevent duplicate notifications today
                            ReminderScheduler.markNotifiedToday(context, relative.id, now)
                        }
                    }
                }

                // Reschedule for tomorrow
                ReminderScheduler.scheduleDailyReminder(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

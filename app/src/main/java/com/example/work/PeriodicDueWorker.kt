package com.example.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that runs periodically (every 2 hours) and immediately on launch
 * to notify the user of any relatives due for contact today.
 */
class PeriodicDueWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val relatives = db.relativeDao().getAllRelativesOnce()

                val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val notifyDue = prefs.getBoolean("pref_notify_due_relatives", true)
                if (!notifyDue) {
                    return@withContext Result.success()
                }

                val userName = prefs.getString("user_name", "") ?: ""
                val lang = prefs.getString("selected_language", "ar") ?: "ar"

                val now = System.currentTimeMillis()

                for (relative in relatives) {
                    val lastContact = relative.lastContactDate
                    val intervalDays = relative.contactIntervalDays

                    val isDue = if (lastContact == 0L) {
                        true // Never called -> Due immediately
                    } else {
                        val diffMs = now - lastContact
                        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                        diffDays >= intervalDays
                    }

                    if (isDue && !ReminderScheduler.wasNotifiedToday(applicationContext, relative.id, now)) {
                        SilaNotificationHelper.sendKinshipNotification(
                            context = applicationContext,
                            relative = relative,
                            lang = lang,
                            userName = userName
                        )

                        ReminderScheduler.markNotifiedToday(applicationContext, relative.id, now)
                    }
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}

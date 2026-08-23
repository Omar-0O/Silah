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

                    if (isDue) {
                        val greeting = when {
                            userName.isNotBlank() && lang == "en" -> "Hey $userName ✨, "
                            userName.isNotBlank() -> "يا $userName 🌸، "
                            else -> ""
                        }

                        val baseMessage = ReminderWorker.buildNotificationMessage(
                            relative.name,
                            relative.relationshipDegree,
                            lang
                        )
                        val notificationMessage = "$greeting$baseMessage"

                        sendDueNotification(
                            relativeName = relative.name,
                            messageText = notificationMessage,
                            relativeId = relative.id,
                            lang = lang
                        )
                    }
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    private fun sendDueNotification(
        relativeName: String,
        messageText: String,
        relativeId: Int,
        lang: String
    ) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "silat_rahim_due_today"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = if (lang == "en") "Due Reminders Today" else "تذكيرات اليوم المستحقة"
                val channelDesc = if (lang == "en") "High priority notification channel for relatives due today" else "قناة تنبيهات عاجلة للأقارب المستحق تواصلهم اليوم"
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = channelDesc
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("relative_id", relativeId)
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                relativeId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (lang == "en") "Sila — Contact Due Today! 🔔" else "صِلَةِ — موعد التواصل اليوم 🔔"

            val iconRes = try {
                com.example.R.drawable.ic_notification_sila
            } catch (e: Exception) {
                com.example.R.mipmap.ic_launcher
            }

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(messageText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(relativeId + 10000, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

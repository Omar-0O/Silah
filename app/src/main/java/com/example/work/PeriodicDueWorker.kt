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
import com.example.NotificationActionReceiver
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that runs periodically (every 2 hours) and immediately on launch
 * to notify the user of any relatives due for contact today.
 * Notification actions (Call, WhatsApp) fire via BroadcastReceiver — no app launch needed.
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

                val appPrefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val silahPrefs = applicationContext.getSharedPreferences("silah_prefs", Context.MODE_PRIVATE)
                val userName = appPrefs.getString("user_name", "") ?: ""
                val lang = appPrefs.getString("selected_language", "ar") ?: "ar"

                val enableCallAction = silahPrefs.getBoolean("notif_action_call", true)
                val enableWhatsappAction = silahPrefs.getBoolean("notif_action_whatsapp", true)
                val enableDoneAction = silahPrefs.getBoolean("notif_action_done", true)

                val now = System.currentTimeMillis()

                for (relative in relatives) {
                    val lastContact = relative.lastContactDate
                    val intervalDays = relative.contactIntervalDays

                    val isDue = if (lastContact == 0L) {
                        true // Never contacted → Due immediately
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
                            relative.name, relative.relationshipDegree, lang
                        )
                        sendDueNotification(
                            relativeName = relative.name,
                            messageText = "$greeting$baseMessage",
                            relativeId = relative.id,
                            phone = relative.phone,
                            lang = lang,
                            enableCallAction = enableCallAction,
                            enableWhatsappAction = enableWhatsappAction,
                            enableDoneAction = enableDoneAction
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
        phone: String,
        lang: String,
        enableCallAction: Boolean,
        enableWhatsappAction: Boolean,
        enableDoneAction: Boolean
    ) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "silat_rahim_due_today"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    if (lang == "en") "Due Reminders Today" else "تذكيرات اليوم المستحقة",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = if (lang == "en") "High priority channel for relatives due today"
                    else "قناة تنبيهات عاجلة للأقارب المستحق تواصلهم اليوم"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Tap → open app
            val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("relative_id", relativeId)
            }
            val openPi = PendingIntent.getActivity(
                applicationContext, relativeId + 10000, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val iconRes = try { com.example.R.drawable.ic_notification_sila }
            catch (e: Exception) { com.example.R.mipmap.ic_launcher }

            val title = if (lang == "en") "❤️ $relativeName needs a call" else "❤️ حان وقت التواصل مع $relativeName"

            val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(messageText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(openPi)
                .setColor(0xFF0E7075.toInt())

            // 📞 Call action via BroadcastReceiver
            if (phone.isNotBlank() && enableCallAction) {
                val callIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_CALL
                    putExtra(NotificationActionReceiver.EXTRA_PHONE, phone)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relativeName)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, relativeId)
                }
                val callPi = PendingIntent.getBroadcast(
                    applicationContext, relativeId + 15000, callIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(iconRes, if (lang == "en") "📞 Call" else "📞 اتصل", callPi)
            }

            // 💬 WhatsApp action via BroadcastReceiver
            if (phone.isNotBlank() && enableWhatsappAction) {
                val waIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_WHATSAPP
                    putExtra(NotificationActionReceiver.EXTRA_PHONE, phone)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relativeName)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, relativeId)
                }
                val waPi = PendingIntent.getBroadcast(
                    applicationContext, relativeId + 17000, waIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(iconRes, if (lang == "en") "💬 WhatsApp" else "💬 واتساب", waPi)
            }

            // ✅ Mark Done → opens app
            if (enableDoneAction) {
                val doneIntent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("relative_id", relativeId)
                    putExtra("action", "mark_contacted")
                }
                val donePi = PendingIntent.getActivity(
                    applicationContext, relativeId + 19000, doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(iconRes, if (lang == "en") "✅ Done" else "✅ تم التواصل", donePi)
            }

            notificationManager.notify(relativeId + 10000, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

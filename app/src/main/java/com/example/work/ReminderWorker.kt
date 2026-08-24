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

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val relativeName = inputData.getString("relative_name") ?: "قريبك"
        val relationshipDegree = inputData.getString("relationship_degree") ?: "أقارب آخرون"
        val relativeId = inputData.getInt("relative_id", -1)

        val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "") ?: ""
        val userGender = prefs.getString("user_gender", "male") ?: "male"
        val lang = prefs.getString("selected_language", "ar") ?: "ar"

        val greeting = when {
            userName.isNotBlank() && lang == "en" -> "Hey $userName ✨, "
            userName.isNotBlank() -> "يا $userName 🌸، "
            else -> ""
        }

        val baseMessage = buildNotificationMessage(relativeName, relationshipDegree, lang)
        val notificationMessage = "$greeting$baseMessage"

        sendNotification(relativeName, notificationMessage, relativeId, lang)

        return Result.success()
    }

    private fun sendNotification(relativeName: String, messageText: String, relativeId: Int, lang: String) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "silat_rahim_reminders"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = if (lang == "en") "Kin Tie Reminders" else "تذكيرات صلة الرحم"
                val channelDesc = if (lang == "en") "Dedicated channel for kin tie connection reminders" else "قناة مخصصة للتذكير بصلة الأرحام والأقارب"
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = channelDesc
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

            val title = if (lang == "en") "Sila — Kin Tie Reminder 🌸" else "صِلَةِ — تذكير بصلة الرحم 🌸"

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
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(relativeId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun buildNotificationMessage(name: String, degree: String, lang: String = "ar"): String {
            val cleanName = name.trim()
            if (lang == "en") {
                return when (degree) {
                    "والدان" -> "It's been a while since you checked on your parents 💚"
                    "أشقاء" -> "Time to connect with your siblings 🌸"
                    "أعمام/أخوال" -> "Don't forget to reach out to your uncles/aunts ✨"
                    else -> "It's time to connect with $cleanName 🌿"
                }
            } else {
                return when (degree) {
                    "والدان" -> {
                        when {
                            cleanName.contains("أم", ignoreCase = true) || cleanName.contains("امي", ignoreCase = true) || cleanName.contains("أمي", ignoreCase = true) || cleanName.contains("والدة", ignoreCase = true) || cleanName.contains("والدتي", ignoreCase = true) ->
                                "بقالك فترة مش بتطمن على والدتك 💚"
                            else -> "بقالك فترة مش بتطمن على والدك 💚"
                        }
                    }
                    "أشقاء" -> {
                        when {
                            cleanName.contains("أخت", ignoreCase = true) || cleanName.contains("اخت", ignoreCase = true) ->
                                "بقالك فترة مش بتطمن على أختك 🌸"
                            else -> "بقالك فترة مش بتطمن على أخوك 🌸"
                        }
                    }
                    "أعمام/أخوال" -> {
                        when {
                            cleanName.contains("خالة", ignoreCase = true) -> "بقالك فترة مش بتطمن على خالتك ✨"
                            cleanName.contains("خال", ignoreCase = true) -> "بقالك فترة مش بتطمن على خالك ✨"
                            cleanName.contains("عمة", ignoreCase = true) -> "بقالك فترة مش بتطمن على عمتك ✨"
                            else -> "بقالك فترة مش بتطمن على عمك ✨"
                        }
                    }
                    else -> {
                        "بقالك فترة مش بتطمن على $cleanName 🌿"
                    }
                }
            }
        }
    }
}

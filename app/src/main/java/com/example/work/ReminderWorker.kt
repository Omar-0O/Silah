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

        val notificationMessage = buildNotificationMessage(relativeName, relationshipDegree)

        sendNotification(relativeName, notificationMessage, relativeId)

        return Result.success()
    }

    private fun sendNotification(relativeName: String, messageText: String, relativeId: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "silat_rahim_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تذكيرات صلة الرحم",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة مخصصة للتذكير بصلة الأرحام والأقارب"
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

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("صِلَةِ — تذكير بصلة الرحم 🌸")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(relativeId, notification)
    }

    companion object {
        fun buildNotificationMessage(name: String, degree: String): String {
            val cleanName = name.trim()
            return when (degree) {
                "والدان" -> {
                    when {
                        cleanName.contains("أم", ignoreCase = true) || cleanName.contains("والدة", ignoreCase = true) ->
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
                    // "أقارب آخرون" أو اسم مخصص — يذكر الاسم المسجل بالكامل
                    "بقالك فترة مش بتطمن على $cleanName 🌿"
                }
            }
        }
    }
}

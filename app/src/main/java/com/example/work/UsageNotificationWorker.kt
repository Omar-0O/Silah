package com.example.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.R
import java.util.concurrent.TimeUnit

class UsageNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = context.getSharedPreferences("silah_prefs", Context.MODE_PRIVATE)

        val notifyEncouragement = prefs.getBoolean("pref_notify_encouragement", true)
        val notifyMonthly = prefs.getBoolean("pref_notify_monthly", true)

        if (!notifyEncouragement && !notifyMonthly) {
            return Result.success()
        }

        val firstLaunchTime = prefs.getLong("app_first_launch_time", System.currentTimeMillis())
        val daysElapsed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - firstLaunchTime)

        val message = when {
            daysElapsed == 7L && notifyEncouragement -> "أسبوع مضى على استخدامك لـ صِلَةِ 💚 شكراً لاتمامك وصل أقاربك!"
            daysElapsed == 30L && notifyEncouragement -> "شهر كامل من الود والارتباط العائلي ✨ صلتك أثرها كبير!"
            daysElapsed == 60L && notifyEncouragement -> "60 يوماً من الوفاء والارتباط 🌿 استمر في وصل الرحم!"
            daysElapsed > 0 && daysElapsed % 30 == 0L && notifyMonthly -> "تذكير صِلَةِ الشهري: طمئن قلب قريب لك اليوم بدعوة أو اتصال 🤍"
            else -> null
        }

        if (message != null) {
            sendNotification(message)
        }

        return Result.success()
    }

    private fun sendNotification(text: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "silah_usage_notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيهات صِلَةِ المشجعة",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val iconRes = try {
            com.example.R.drawable.ic_notification_sila
        } catch (e: Exception) {
            com.example.R.mipmap.ic_launcher
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle("صِلَةِ 🤍")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2001, notification)
    }
}

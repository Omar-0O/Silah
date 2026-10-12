package com.example.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.datastore.UserPreferencesRepository
import com.example.core.util.SilaLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = userPreferencesRepository.getInitialPreferences()

            val notifyEncouragement = prefs.prefNotifyEncouragement
            val notifyMonthly = prefs.prefNotifyMonthly

            if (!notifyEncouragement && !notifyMonthly) {
                return Result.success()
            }

            val firstLaunchTime = if (prefs.appFirstLaunchTime != 0L) prefs.appFirstLaunchTime else System.currentTimeMillis()
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

            Result.success()
        } catch (e: Exception) {
            SilaLogger.e("UsageNotificationWorker", "Error sending usage notification", e)
            Result.failure()
        }
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

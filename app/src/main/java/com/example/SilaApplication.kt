package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.work.ReminderScheduler

/**
 * Custom Application class for Sila.
 * Pre-configures notification channels and ensures reminder scheduling is initialized
 * whenever any app component (Activity, Receiver, or Service) starts.
 */
class SilaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
        ReminderScheduler.scheduleDailyReminder(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelDueToday = NotificationChannel(
                "silat_rahim_due_today",
                "تذكيرات صلة الرحم اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة تنبيهات لتذكيرك بالتواصل مع أرحامك اليوم"
                enableVibration(true)
            }

            val channelReminders = NotificationChannel(
                "silat_rahim_reminders",
                "تذكيرات صلة الرحم",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة مخصصة للتذكير بصلة الأرحام والأقارب"
                enableVibration(true)
            }

            val channelUsage = NotificationChannel(
                "silat_rahim_usage",
                "إحصائيات وتشجيع صِلَة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "قناة الإشعارات الدورية والتشجيعية"
            }

            notificationManager.createNotificationChannels(
                listOf(channelDueToday, channelReminders, channelUsage)
            )
        }
    }
}

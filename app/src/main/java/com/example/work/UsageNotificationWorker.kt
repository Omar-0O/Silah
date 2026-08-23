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

class UsageNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

                val notifyEncouragement = prefs.getBoolean("pref_notify_encouragement", true)
                val notifyMonthly = prefs.getBoolean("pref_notify_monthly", true)
                val notifyDue = prefs.getBoolean("pref_notify_due_relatives", true)
                val lang = prefs.getString("selected_language", "ar") ?: "ar"

                var firstLaunchTime = prefs.getLong("app_first_launch_time", 0L)
                val now = System.currentTimeMillis()

                if (firstLaunchTime == 0L) {
                    firstLaunchTime = now
                    prefs.edit().putLong("app_first_launch_time", now).apply()
                    return@withContext Result.success()
                }

                val daysDiff = ((now - firstLaunchTime) / (1000 * 60 * 60 * 24)).toInt()

                // Track sent usage notification milestones to avoid duplicates
                val sentMilestones = prefs.getStringSet("sent_usage_notifications", emptySet()) ?: emptySet()

                var title = if (lang == "en") "Sila — Connection Reminder 🌸" else "صِلَةِ — تذكير صلة الرحم 🌸"
                var bodyText: String? = null
                var notificationId = 7000
                var milestoneKey: String? = null

                if (daysDiff in 7..13 && !sentMilestones.contains("usage_week_1")) {
                    if (notifyEncouragement) {
                        bodyText = if (lang == "en") {
                            "🤍 One week with Sila!\nWho haven't you reached out to in a while?\nA quick call could mean the world to them."
                        } else {
                            "🤍 بقالك أسبوع مع صِلَة!\nمين الشخص اللي بقالك فترة مسألتش عليه؟\nيمكن مكالمة صغيرة تفرق معاه جدًا."
                        }
                        milestoneKey = "usage_week_1"
                        notificationId = 7001
                    }
                } else if (daysDiff in 30..44 && !sentMilestones.contains("usage_month_1")) {
                    if (notifyEncouragement) {
                        bodyText = if (lang == "en") {
                            "🌱 One month with Sila!\nKeep connecting with family, even a short call brings hearts closer."
                        } else {
                            "🌱 شهر مع صِلَة!\nاستمر في السؤال عن أهلك، حتى مكالمة قصيرة ممكن تقرب المسافات."
                        }
                        milestoneKey = "usage_month_1"
                        notificationId = 7002
                    }
                } else if (daysDiff in 60..75 && !sentMilestones.contains("usage_month_2")) {
                    if (notifyEncouragement) {
                        bodyText = if (lang == "en") {
                            "🤍 Two months with Sila!\nBeautiful relationships thrive on continuous care.\nRemember a relative today."
                        } else {
                            "🤍 شهرين من صِلَة!\nالعلاقات الجميلة محتاجة سؤال مستمر.\nافتكر حد من أهلك النهارده."
                        }
                        milestoneKey = "usage_month_2"
                        notificationId = 7003
                    }
                } else if (daysDiff > 75) {
                    val lastMonthlyTime = prefs.getLong("last_monthly_notification_time", 0L)
                    val daysSinceLastMonthly = ((now - lastMonthlyTime) / (1000 * 60 * 60 * 24)).toInt()

                    if (daysSinceLastMonthly >= 28 && notifyMonthly) {
                        val templateIndex = prefs.getInt("monthly_template_index", 0)
                        bodyText = getMonthlyTemplate(templateIndex, lang)
                        
                        // Smart notification check if enabled: append overdue relative if exists
                        if (notifyDue) {
                            val db = AppDatabase.getDatabase(applicationContext)
                            val overdueRelative = db.relativeDao().getAllRelativesOnce().firstOrNull { relative ->
                                if (relative.lastContactDate == 0L) true
                                else {
                                    val diff = now - relative.lastContactDate
                                    diff >= (relative.contactIntervalDays * 86_400_000L)
                                }
                            }
                            if (overdueRelative != null) {
                                bodyText = if (lang == "en") {
                                    "🤍 You haven't reached out to ${overdueRelative.name} in a while.\nWould you like to check on them today?"
                                } else {
                                    "🤍 بقالك فترة مسألتش على ${overdueRelative.name}.\nتحب تطمن عليه؟"
                                }
                            }
                        }

                        val nextIndex = (templateIndex + 1) % 5
                        prefs.edit()
                            .putLong("last_monthly_notification_time", now)
                            .putInt("monthly_template_index", nextIndex)
                            .apply()
                        notificationId = 7100 + templateIndex
                    }
                }

                if (bodyText != null) {
                    sendUsageNotification(title, bodyText, notificationId, lang)
                    if (milestoneKey != null) {
                        val updated = sentMilestones.toMutableSet().apply { add(milestoneKey) }
                        prefs.edit().putStringSet("sent_usage_notifications", updated).apply()
                    }
                }

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    private fun getMonthlyTemplate(index: Int, lang: String): String {
        return if (lang == "en") {
            when (index) {
                0 -> "🤍 Remember your family today.\nA small call can make a big difference."
                1 -> "Who was the last relative you spoke with?\nToday might be the perfect time to check in."
                2 -> "Connecting with family doesn't take much time...\nIt just takes remembering each other. 🤍"
                3 -> "Someone might be waiting for your call.\nWho will be the first person you call today?"
                else -> "🌱 Preserve your ties.\nA simple message can bring joy to someone you love."
            }
        } else {
            when (index) {
                0 -> "🤍 افتكر أهلك النهارده.\nمكالمة صغيرة ممكن تعمل فرق كبير."
                1 -> "مين آخر شخص من أهلك كلمته؟\nيمكن النهارده يكون وقت مناسب تسأل عليه."
                2 -> "صلة الرحم مش محتاجة وقت كبير...\nمحتاجة إننا نفتكر بعض. 🤍"
                3 -> "شخص واحد ممكن يكون مستني مكالمتك.\nمين هيكون أول واحد تكلمه النهارده؟"
                else -> "🌱 حافظ على صِلَتك.\nرسالة بسيطة منك ممكن تفرّح حد بتحبه."
            }
        }
    }

    private fun sendUsageNotification(title: String, messageText: String, notificationId: Int, lang: String) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "silat_rahim_usage_reminders"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = if (lang == "en") "Usage & Encouragement Messages" else "رسائل التشجيع والتنبيهات"
                val channelDesc = if (lang == "en") "Channel for milestone encouragement and periodic kin tie reminders" else "قناة تنبيهات مخصصة لرسائل التشجيع والتذكيرات الشهرية"
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = channelDesc
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

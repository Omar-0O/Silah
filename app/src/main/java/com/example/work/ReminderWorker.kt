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
import com.example.R

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val relativeName = inputData.getString("relative_name") ?: "قريبك"
        val relationshipDegree = inputData.getString("relationship_degree") ?: "أقارب آخرون"
        val relativeId = inputData.getInt("relative_id", -1)
        val relativePhone = inputData.getString("relative_phone") ?: ""

        val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "") ?: ""
        val lang = prefs.getString("selected_language", "ar") ?: "ar"

        val greeting = when {
            userName.isNotBlank() && lang == "en" -> "Hey $userName, "
            userName.isNotBlank() -> "يا $userName، "
            else -> ""
        }

        val baseMessage = buildNotificationMessage(relativeName, relationshipDegree, lang)
        val notificationMessage = "$greeting$baseMessage"

        sendNotification(relativeName, notificationMessage, relativeId, relativePhone, lang)
        return Result.success()
    }

    private fun sendNotification(
        relativeName: String,
        messageText: String,
        relativeId: Int,
        phone: String,
        lang: String
    ) {
        try {
            val silahPrefs = applicationContext.getSharedPreferences("silah_prefs", Context.MODE_PRIVATE)
            val enableCallAction = silahPrefs.getBoolean("notif_action_call", true)
            val enableWhatsappAction = silahPrefs.getBoolean("notif_action_whatsapp", true)
            val enableDoneAction = silahPrefs.getBoolean("notif_action_done", true)

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "silat_rahim_reminders"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    if (lang == "en") "Kin Tie Reminders" else "تذكيرات صلة الرحم",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = if (lang == "en") "Reminders to stay connected with your relatives"
                    else "تذكيرات للتواصل مع أقاربك"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Tap notification → open app
            val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("relative_id", relativeId)
                putExtra("open_tab", "relatives")
            }
            val openPendingIntent = PendingIntent.getActivity(
                applicationContext, relativeId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val iconRes = try { R.drawable.ic_notification_sila } catch (e: Exception) { R.mipmap.ic_launcher }
            val title = if (lang == "en") "❤️ Time to connect with $relativeName"
            else "❤️ حان وقت تواصلك مع $relativeName"

            val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(messageText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setColor(0xFF0E7075.toInt())

            // 📞 Call — BroadcastReceiver fires directly (no app launch)
            if (phone.isNotBlank() && enableCallAction) {
                val callIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_CALL
                    putExtra(NotificationActionReceiver.EXTRA_PHONE, phone)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relativeName)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, relativeId)
                }
                val callPi = PendingIntent.getBroadcast(
                    applicationContext, relativeId + 5000, callIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(iconRes, if (lang == "en") "📞 Call" else "📞 اتصل", callPi)
            }

            // 💬 WhatsApp — BroadcastReceiver fires directly
            if (phone.isNotBlank() && enableWhatsappAction) {
                val waIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_WHATSAPP
                    putExtra(NotificationActionReceiver.EXTRA_PHONE, phone)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relativeName)
                    putExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, relativeId)
                }
                val waPi = PendingIntent.getBroadcast(
                    applicationContext, relativeId + 7000, waIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(iconRes, if (lang == "en") "💬 WhatsApp" else "💬 واتساب", waPi)
            }

            // ✅ Mark Done — opens app to confirm
            if (enableDoneAction) {
                val doneIntent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("relative_id", relativeId)
                    putExtra("action", "mark_contacted")
                }
                val donePi = PendingIntent.getActivity(
                    applicationContext, relativeId + 9000, doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(iconRes, if (lang == "en") "✅ Done" else "✅ تم التواصل", donePi)
            }

            notificationManager.notify(relativeId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun buildNotificationMessage(name: String, degree: String, lang: String = "ar"): String {
            val n = name.trim()
            if (lang == "en") {
                return when (degree) {
                    "أب", "أم" -> "It's been a while — check on your parent 💚"
                    "جد", "جدة" -> "Don't forget your grandparent — they miss you ❤️"
                    "أخ", "أخت" -> "Reach out to your sibling today 🌸"
                    "عم", "عمة" -> "It's time to connect with your uncle/aunt ✨"
                    "خال", "خالة" -> "Stay in touch with your maternal uncle/aunt ✨"
                    else -> "Reach out to $n today 🌿"
                }
            } else {
                return when (degree) {
                    "أم" -> "بقالك فترة مش بتطمن على أمك 💚"
                    "أب" -> "بقالك فترة مش بتطمن على أبوك 💚"
                    "جدة" -> "بقالك فترة مش بتطمن على جدتك ❤️"
                    "جد" -> "بقالك فترة مش بتطمن على جدك ❤️"
                    "أخت" -> "بقالك فترة مش بتطمن على أختك 🌸"
                    "أخ" -> "بقالك فترة مش بتطمن على أخوك 🌸"
                    "عمة" -> "بقالك فترة مش بتطمن على عمتك ✨"
                    "عم" -> "بقالك فترة مش بتطمن على عمك ✨"
                    "خالة" -> "بقالك فترة مش بتطمن على خالتك ✨"
                    "خال" -> "بقالك فترة مش بتطمن على خالك ✨"
                    else -> "بقالك فترة مش بتطمن على $n 🌿"
                }
            }
        }
    }
}

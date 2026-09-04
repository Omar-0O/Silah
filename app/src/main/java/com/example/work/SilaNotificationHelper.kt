package com.example.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.Relative

/**
 * Central builder for high-quality, degree-aware Kinship notifications with interactive actions.
 */
object SilaNotificationHelper {

    const val CHANNEL_ID_DUE = "silat_rahim_due_today"
    const val CHANNEL_ID_REMINDERS = "silat_rahim_reminders"

    fun ensureChannels(context: Context, lang: String = "ar") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelDue = NotificationChannel(
                CHANNEL_ID_DUE,
                if (lang == "en") "Daily Kinship Reminders" else "تذكيرات صلة الرحم اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = if (lang == "en") "Daily reminders to connect with your relatives" else "قناة تنبيهات لتذكيرك بالتواصل مع أرحامك اليوم"
                enableVibration(true)
            }

            val channelReminders = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                if (lang == "en") "General Kinship Reminders" else "تذكيرات صلة الرحم العامة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = if (lang == "en") "General reminders for kinship connection" else "قناة تذكيرات دورية بصلة الأرحام"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(channelDue, channelReminders))
        }
    }

    /**
     * Builds and sends a rich degree-aware notification with 3 interactive action buttons:
     * 1. 📞 Call
     * 2. 💬 Message / WhatsApp
     * 3. ✅ Mark Contacted (Done)
     */
    fun sendKinshipNotification(
        context: Context,
        relative: Relative,
        lang: String = "ar",
        userName: String = ""
    ) {
        try {
            ensureChannels(context, lang)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            val fullMessage = "$greeting$baseMessage"

            val title = if (lang == "en") {
                when (relative.relationshipDegree) {
                    "والدان" -> "Parents Kinship 💚"
                    "أشقاء" -> "Siblings Connection 🌸"
                    "أعمام/أخوال" -> "Uncles & Aunts Kinship ✨"
                    else -> "Family Kinship Reminder 🌿"
                }
            } else {
                when (relative.relationshipDegree) {
                    "والدان" -> "بِرّ الوالدين 💚"
                    "أشقاء" -> "صلة الإخوة والأخوات 🌸"
                    "أعمام/أخوال" -> "صلة الأرحام والأعمام ✨"
                    else -> "تذكير بصلة الرحم 🌿"
                }
            }

            val subText = if (lang == "en") "Sila • Kinship Time" else "صِلَةِ • موعد صلة الرحم 🌿"

            // Primary Sage Green color from Sila Design System
            val brandColor = 0xFF2D5A3D.toInt()

            // ── Tap Notification Body: Opens MainActivity at relative detail screen ──
            val contentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("relative_id", relative.id)
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                relative.id,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_DUE)
                .setSmallIcon(R.drawable.ic_notification_sila)
                .setColor(brandColor)
                .setColorized(true)
                .setContentTitle(title)
                .setContentText(fullMessage)
                .setSubText(subText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText("$fullMessage\n\n${if (lang == "en") "Connecting brings joy and barakah to your family." else "تواصلك اليوم يُدخل السرور والبركة على قلوب أهلك 🌿"}")
                        .setSummaryText(subText)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)

            // ── Action 1: Direct Call 📞 ──────────────────────────────────────────
            if (relative.phone.isNotBlank()) {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${relative.phone}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val dialPendingIntent = PendingIntent.getActivity(
                    context,
                    relative.id * 10 + 1,
                    dialIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_call,
                    if (lang == "en") "Call 📞" else "اتصال 📞",
                    dialPendingIntent
                )
            }

            // ── Action 2: Direct WhatsApp / Message 💬 ────────────────────────────
            if (relative.phone.isNotBlank()) {
                val messageIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_MESSAGE
                    putExtra(NotificationActionReceiver.EXTRA_PHONE, relative.phone)
                }
                val messagePendingIntent = PendingIntent.getBroadcast(
                    context,
                    relative.id * 10 + 2,
                    messageIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_send,
                    if (lang == "en") "Message 💬" else "مراسلة 💬",
                    messagePendingIntent
                )
            }

            // ── Action 3: Mark as Contacted (Done) in Background ✅ ───────────────
            val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_CONTACTED
                putExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, relative.id)
                putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relative.name)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, relative.id + 10000)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                context,
                relative.id * 10 + 3,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.checkbox_on_background,
                if (lang == "en") "Mark Done ✅" else "تم التواصل ✅",
                donePendingIntent
            )

            notificationManager.notify(relative.id + 10000, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

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
import com.example.work.NotificationStyleEngine.NotificationStyle

/**
 * Central builder for high-quality, context-aware Kinship notifications with interactive actions.
 * Uses [NotificationStyleEngine] to pick the right style (Heartwarming, Gentle, Friday, Ramadan, Eid).
 */
object SilaNotificationHelper {

    const val CHANNEL_ID_DUE = "silat_rahim_due_today"
    const val CHANNEL_ID_REMINDERS = "silat_rahim_reminders"

    /** Snooze duration in milliseconds — 6 hours */
    const val SNOOZE_DURATION_MS = 6 * 60 * 60 * 1000L

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
     * Builds and sends a context-aware notification with style-specific action buttons.
     * The style is automatically resolved by [NotificationStyleEngine].
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

            // ── Resolve notification content via style engine ──
            val content = NotificationStyleEngine.generateContent(relative.name, lang)
            val title = if (lang == "en") content.titleEn else content.titleAr
            val body = if (lang == "en") content.bodyEn else content.bodyAr

            // Primary Green from Sila Design System
            val brandColor = 0xFF1E5A35.toInt()

            // ── Tap Notification Body: Opens MainActivity at relative detail ──
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

            val notificationId = relative.id + 10000

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_DUE)
                .setSmallIcon(R.drawable.ic_notification_sila)
                .setColor(brandColor)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)

            // ── Add style-specific action buttons ──
            addActionsForStyle(context, builder, content.style, relative, notificationId, lang)

            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Adds action buttons to the notification based on the resolved style.
     */
    private fun addActionsForStyle(
        context: Context,
        builder: NotificationCompat.Builder,
        style: NotificationStyle,
        relative: Relative,
        notificationId: Int,
        lang: String
    ) {
        when (style) {
            // ── النمط 1: دقيقة تسعد قلب ──
            // Buttons: 📞 اتصال مباشر · 💬 واتساب · ⏰ ذكّرني لاحقاً
            NotificationStyle.HEARTWARMING -> {
                addCallAction(context, builder, relative, lang)
                addWhatsAppAction(context, builder, relative, lang)
                addSnoozeAction(context, builder, relative, notificationId, lang)
            }

            // ── النمط 2: لمسة وجدانية ──
            // Buttons: 📱 تواصل الآن · ⏳ بعد 6 ساعات
            NotificationStyle.GENTLE_EMOTIONAL -> {
                addOpenRelativeAction(context, builder, relative, lang)
                addSnoozeAction(context, builder, relative, notificationId, lang,
                    labelAr = "⏳ بعد 6 ساعات", labelEn = "⏳ In 6 Hours")
            }

            // ── النمط 3: الجمعة ──
            // Button: 📇 فتح قائمة الأقارب
            NotificationStyle.FRIDAY -> {
                addOpenRelativesListAction(context, builder, lang)
            }

            // ── النمط 4: رمضان ──
            // Buttons: 📞 اتصال مباشر · 💬 واتساب · ⏰ ذكّرني لاحقاً
            NotificationStyle.RAMADAN -> {
                addCallAction(context, builder, relative, lang)
                addWhatsAppAction(context, builder, relative, lang)
                addSnoozeAction(context, builder, relative, notificationId, lang)
            }

            // ── النمط 5: العيد ──
            // Buttons: 📞 اتصال مباشر · 💬 واتساب
            NotificationStyle.EID -> {
                addCallAction(context, builder, relative, lang)
                addWhatsAppAction(context, builder, relative, lang)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Action Builders
    // ═══════════════════════════════════════════════════════════════════════════

    /** 📞 Direct call — opens dialer with relative's phone number */
    private fun addCallAction(
        context: Context,
        builder: NotificationCompat.Builder,
        relative: Relative,
        lang: String
    ) {
        if (relative.phone.isBlank()) return

        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${relative.phone}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            relative.id * 10 + 1,
            dialIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_notification_sila,
            if (lang == "en") "📞 Call Now" else "📞 اتصال مباشر",
            pendingIntent
        )
    }

    /** 💬 WhatsApp — opens WhatsApp with a pre-filled greeting message */
    private fun addWhatsAppAction(
        context: Context,
        builder: NotificationCompat.Builder,
        relative: Relative,
        lang: String
    ) {
        if (relative.phone.isBlank()) return

        val messageIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MESSAGE
            putExtra(NotificationActionReceiver.EXTRA_PHONE, relative.phone)
            putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relative.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            relative.id * 10 + 2,
            messageIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_notification_sila,
            if (lang == "en") "💬 WhatsApp" else "💬 واتساب",
            pendingIntent
        )
    }

    /** ⏰ Snooze — dismisses notification and reschedules after 6 hours */
    private fun addSnoozeAction(
        context: Context,
        builder: NotificationCompat.Builder,
        relative: Relative,
        notificationId: Int,
        lang: String,
        labelAr: String = "⏰ ذكّرني لاحقاً",
        labelEn: String = "⏰ Remind Later"
    ) {
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, relative.id)
            putExtra(NotificationActionReceiver.EXTRA_RELATIVE_NAME, relative.name)
            putExtra(NotificationActionReceiver.EXTRA_PHONE, relative.phone)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            relative.id * 10 + 4,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_notification_sila,
            if (lang == "en") labelEn else labelAr,
            pendingIntent
        )
    }

    /** 📱 Open relative's detail page in the app */
    private fun addOpenRelativeAction(
        context: Context,
        builder: NotificationCompat.Builder,
        relative: Relative,
        lang: String
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("relative_id", relative.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            relative.id * 10 + 5,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_notification_sila,
            if (lang == "en") "📱 Connect Now" else "📱 تواصل الآن",
            pendingIntent
        )
    }

    /** 📇 Open the relatives list tab in the app */
    private fun addOpenRelativesListAction(
        context: Context,
        builder: NotificationCompat.Builder,
        lang: String
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "relatives")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_notification_sila,
            if (lang == "en") "📇 Open Relatives List" else "📇 فتح قائمة الأقارب",
            pendingIntent
        )
    }
}

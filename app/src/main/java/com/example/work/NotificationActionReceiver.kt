package com.example.work

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import com.example.data.AppDatabase
import com.example.data.CommunicationLog
import com.example.widget.SilaAppWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles interactive action clicks from Kinship notifications:
 * - 📞 Call (dial phone)
 * - 💬 WhatsApp (with pre-filled greeting)
 * - ⏰ Snooze (dismiss + reschedule after 6 hours)
 * - ✅ Mark Contacted (record in DB)
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CALL = "com.example.silatrahim.ACTION_CALL"
        const val ACTION_MESSAGE = "com.example.silatrahim.ACTION_MESSAGE"
        const val ACTION_MARK_CONTACTED = "com.example.silatrahim.ACTION_MARK_CONTACTED"
        const val ACTION_SNOOZE = "com.example.silatrahim.ACTION_SNOOZE"

        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_RELATIVE_ID = "extra_relative_id"
        const val EXTRA_RELATIVE_NAME = "extra_relative_name"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val phone = intent.getStringExtra(EXTRA_PHONE) ?: ""
        val relativeId = intent.getIntExtra(EXTRA_RELATIVE_ID, -1)
        val relativeName = intent.getStringExtra(EXTRA_RELATIVE_NAME) ?: "القريب"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        when (action) {
            ACTION_CALL -> {
                if (phone.isNotBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                }
            }

            ACTION_MESSAGE -> {
                if (phone.isNotBlank()) {
                    val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
                    // Pre-filled greeting message
                    val greetingText = "السلام عليكم، اطمنت عليك 🤍"
                    val encodedText = Uri.encode(greetingText)
                    val waIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(waIntent)
                    } catch (e: Exception) {
                        // Fallback to SMS if WhatsApp is not installed
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("sms_body", greetingText)
                        }
                        try {
                            context.startActivity(smsIntent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                // 1. Dismiss the current notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationId != -1) {
                    notificationManager.cancel(notificationId)
                } else if (relativeId != -1) {
                    notificationManager.cancel(relativeId + 10000)
                }

                // 2. Schedule a new alarm after 6 hours
                if (relativeId != -1) {
                    scheduleSnoozeAlarm(context, relativeId, relativeName, phone)

                    val lang = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                        .getString("selected_language", "ar") ?: "ar"

                    Handler(Looper.getMainLooper()).post {
                        val toastMsg = if (lang == "en") "Reminder snoozed for 6 hours ⏰"
                        else "هنذكرك بعد 6 ساعات ⏰"
                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            ACTION_MARK_CONTACTED -> {
                if (relativeId != -1) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val relative = db.relativeDao().getRelativeById(relativeId)
                            val now = System.currentTimeMillis()

                            // 1. Insert log
                            val log = CommunicationLog(
                                relativeId = relativeId,
                                type = "مكالمة هاتفية",
                                notes = "تم تسجيل التواصل من شريط الإشعارات",
                                timestamp = now
                            )
                            db.communicationLogDao().insertLog(log)

                            // 2. Update relative last contact date
                            if (relative != null) {
                                db.relativeDao().updateRelative(relative.copy(lastContactDate = now))
                            }

                            // 3. Mark notified today and remove from pending check-in confirmation
                            ReminderScheduler.markNotifiedToday(context, relativeId, now)
                            ReminderScheduler.removePendingNotifiedRelative(context, relativeId)

                            // 4. Cancel active notification
                            val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (notificationId != -1) {
                                notifManager.cancel(notificationId)
                            } else {
                                notifManager.cancel(relativeId + 10000)
                            }

                            // 5. Update widgets
                            SilaAppWidgetProvider.triggerWidgetUpdate(context)

                            // 6. Show toast on main thread
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "تم تسجيل التواصل مع $relativeName بنجاح 💚",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }

    /**
     * Schedules a one-shot alarm to re-trigger the kinship notification after [SilaNotificationHelper.SNOOZE_DURATION_MS].
     * Uses [SnoozeAlarmReceiver] to fire the snoozed notification.
     */
    private fun scheduleSnoozeAlarm(
        context: Context,
        relativeId: Int,
        relativeName: String,
        phone: String
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerTime = System.currentTimeMillis() + SilaNotificationHelper.SNOOZE_DURATION_MS

            val snoozeIntent = Intent(context, SnoozeAlarmReceiver::class.java).apply {
                putExtra(EXTRA_RELATIVE_ID, relativeId)
                putExtra(EXTRA_RELATIVE_NAME, relativeName)
                putExtra(EXTRA_PHONE, phone)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                relativeId + 20000,  // Unique request code for snooze
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    AlarmManagerCompat.setExactAndAllowWhileIdle(
                        alarmManager, AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                } else {
                    AlarmManagerCompat.setAndAllowWhileIdle(
                        alarmManager, AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                AlarmManagerCompat.setAndAllowWhileIdle(
                    alarmManager, AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

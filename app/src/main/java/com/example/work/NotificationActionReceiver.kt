package com.example.work

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.data.CommunicationLog
import com.example.widget.SilaAppWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles interactive action clicks from Kinship notifications (Call, WhatsApp, Mark Contacted).
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CALL = "com.example.silatrahim.ACTION_CALL"
        const val ACTION_MESSAGE = "com.example.silatrahim.ACTION_MESSAGE"
        const val ACTION_MARK_CONTACTED = "com.example.silatrahim.ACTION_MARK_CONTACTED"

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
                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(waIntent)
                    } catch (e: Exception) {
                        // Fallback to SMS if WhatsApp is not installed
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(smsIntent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
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
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (notificationId != -1) {
                                notificationManager.cancel(notificationId)
                            } else {
                                notificationManager.cancel(relativeId + 10000)
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
}

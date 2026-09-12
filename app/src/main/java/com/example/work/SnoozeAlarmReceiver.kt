package com.example.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fired by AlarmManager after a snooze period (6 hours).
 * Re-sends the kinship notification for the snoozed relative.
 */
class SnoozeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val relativeId = intent?.getIntExtra(NotificationActionReceiver.EXTRA_RELATIVE_ID, -1) ?: -1
        if (relativeId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val relative = db.relativeDao().getRelativeById(relativeId) ?: return@launch

                val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val userName = prefs.getString("user_name", "") ?: ""
                val lang = prefs.getString("selected_language", "ar") ?: "ar"

                SilaNotificationHelper.sendKinshipNotification(
                    context = context,
                    relative = relative,
                    lang = lang,
                    userName = userName
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

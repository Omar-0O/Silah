package com.example.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase

/**
 * WorkManager periodic worker for individual relative reminders.
 * Delegates to [SilaNotificationHelper] to send the actual notification,
 * and checks [ReminderScheduler.wasNotifiedToday] to prevent duplicates
 * with the AlarmReceiver/PeriodicDueWorker path.
 */
class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val relativeId = inputData.getInt("relative_id", -1)
        if (relativeId == -1) return Result.success()

        val now = System.currentTimeMillis()

        // ── Prevent duplicate: if already notified today, skip ──
        if (ReminderScheduler.wasNotifiedToday(applicationContext, relativeId, now)) {
            return Result.success()
        }

        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val relative = db.relativeDao().getRelativeById(relativeId) ?: return Result.success()

            // Check if actually due
            val isDue = if (relative.lastContactDate == 0L) {
                true
            } else {
                val diffDays = ((now - relative.lastContactDate) / (1000 * 60 * 60 * 24)).toInt()
                diffDays >= relative.contactIntervalDays
            }

            if (!isDue) return Result.success()

            val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val userName = prefs.getString("user_name", "") ?: ""
            val lang = prefs.getString("selected_language", "ar") ?: "ar"

            // ── Send notification through the central helper ──
            SilaNotificationHelper.sendKinshipNotification(
                context = applicationContext,
                relative = relative,
                lang = lang,
                userName = userName
            )

            // ── Mark as notified today to prevent duplicates from AlarmReceiver/PeriodicDueWorker ──
            ReminderScheduler.markNotifiedToday(applicationContext, relativeId, now)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.success()
    }

    companion object {
        /**
         * Builds a degree-aware notification message.
         * Kept for backward compatibility with any external callers.
         */
        fun buildNotificationMessage(name: String, degree: String, lang: String = "ar"): String {
            val cleanName = name.trim()
            if (lang == "en") {
                return when (degree) {
                    "والدان", "Parents" -> "It's been a while since you checked on your parents 💚"
                    "أشقاء", "Siblings" -> "Time to connect with your siblings 🌸"
                    "أعمام/أخوال", "Uncles/Aunts" -> "Don't forget to reach out to your uncles/aunts ✨"
                    "أب", "أم" -> "It's been a while — check on your parent 💚"
                    "جد", "جدة" -> "Don't forget your grandparent — they miss you ❤️"
                    "أخ", "أخت" -> "Reach out to your sibling today 🌸"
                    "عم", "عمة" -> "It's time to connect with your uncle/aunt ✨"
                    "خال", "خالة" -> "Stay in touch with your maternal uncle/aunt ✨"
                    else -> "It's time to connect with $cleanName 🌿"
                }
            } else {
                return when (degree) {
                    "والدان" -> {
                        when {
                            cleanName.contains("أم", ignoreCase = true) ||
                            cleanName.contains("امي", ignoreCase = true) ||
                            cleanName.contains("أمي", ignoreCase = true) ||
                            cleanName.contains("والدة", ignoreCase = true) ||
                            cleanName.contains("والدتي", ignoreCase = true) ->
                                "بقالك فترة مش بتطمن على والدتك 💚"
                            else -> "بقالك فترة مش بتطمن على والدك 💚"
                        }
                    }
                    "أشقاء" -> {
                        when {
                            cleanName.contains("أخت", ignoreCase = true) ||
                            cleanName.contains("اخت", ignoreCase = true) ->
                                "بقالك فترة مش بتطمن على أختك 🌸"
                            else -> "بقالك فترة مش بتطمن على أخوك 🌸"
                        }
                    }
                    "أعمام/أخوال" -> {
                        when {
                            cleanName.contains("خالة", ignoreCase = true) -> "بقالك فترة مش بتطمن على خالتك ✨"
                            cleanName.contains("خال", ignoreCase = true) -> "بقالك فترة مش بتطمن على خالك ✨"
                            cleanName.contains("عمة", ignoreCase = true) -> "بقالك فترة مش بتطمن على عمتك ✨"
                            else -> "بقالك فترة مش بتطمن على عمك ✨"
                        }
                    }
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
                    else -> "بقالك فترة مش بتطمن على $cleanName 🌿"
                }
            }
        }
    }
}

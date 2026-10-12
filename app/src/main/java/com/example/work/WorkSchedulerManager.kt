package com.example.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.util.SilaLogger
import com.example.data.Relative
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized manager for all WorkManager scheduling, resilience, and constraints.
 * Ensures idempotent scheduling with [ExistingPeriodicWorkPolicy.UPDATE] / [ExistingPeriodicWorkPolicy.KEEP]
 * and respects Android 12+ / Doze battery optimizations.
 */
@Singleton
class WorkSchedulerManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    companion object {
        const val WORK_PERIODIC_DUE = "sila_periodic_due_check"
        const val WORK_IMMEDIATE_DUE = "sila_immediate_due_check"
        const val WORK_CALL_LOG_SYNC = "sila_periodic_call_log_sync"
        const val WORK_AUTO_BACKUP = "sila_periodic_auto_backup"
        const val WORK_USAGE_NOTIFICATIONS = "sila_periodic_usage_notifications"

        val BatteryNotLowConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val StorageNotLowConstraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()
    }

    /**
     * Initializes all baseline background workers idempotently on app launch.
     */
    fun initializeDefaultWorkers() {
        schedulePeriodicDueCheck()
        scheduleCallLogSync()
        scheduleAutoBackup()
        scheduleUsageNotifications()
    }

    /**
     * Schedules periodic due-check worker every 2 hours with battery constraints.
     */
    fun schedulePeriodicDueCheck() {
        try {
            val request = PeriodicWorkRequestBuilder<PeriodicDueWorker>(2, TimeUnit.HOURS)
                .setConstraints(BatteryNotLowConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_PERIODIC_DUE,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            SilaLogger.i("WorkSchedulerManager", "Enqueued $WORK_PERIODIC_DUE")
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to schedule periodic due check", e)
        }
    }

    /**
     * Triggers an immediate one-time check for due relatives.
     */
    fun triggerImmediateDueCheck() {
        try {
            val request = OneTimeWorkRequestBuilder<PeriodicDueWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniqueWork(
                WORK_IMMEDIATE_DUE,
                ExistingWorkPolicy.REPLACE,
                request
            )
            SilaLogger.i("WorkSchedulerManager", "Triggered immediate due check")
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to trigger immediate due check", e)
        }
    }

    /**
     * Schedules individual relative reminder recurring based on [Relative.contactIntervalDays].
     */
    fun scheduleRelativeReminder(relative: Relative) {
        try {
            val data = Data.Builder()
                .putInt("relative_id", relative.id)
                .putString("relative_name", relative.name)
                .putString("relationship_degree", relative.relationshipDegree)
                .build()

            val intervalDays = maxOf(1L, relative.contactIntervalDays.toLong())
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalDays, TimeUnit.DAYS)
                .setInputData(data)
                .setConstraints(BatteryNotLowConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "reminder_${relative.id}",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            SilaLogger.i("WorkSchedulerManager", "Scheduled reminder for relative: ${relative.name} (${relative.id})")
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to schedule reminder for relative ${relative.id}", e)
        }
    }

    /**
     * Cancels an individual relative reminder.
     */
    fun cancelRelativeReminder(relativeId: Int) {
        try {
            workManager.cancelUniqueWork("reminder_$relativeId")
            SilaLogger.i("WorkSchedulerManager", "Cancelled reminder for relative $relativeId")
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to cancel reminder for relative $relativeId", e)
        }
    }

    /**
     * Schedules background call-log synchronization once every 12 hours.
     */
    fun scheduleCallLogSync(periodic: Boolean = true) {
        try {
            if (periodic) {
                val request = PeriodicWorkRequestBuilder<CallLogSyncWorker>(12, TimeUnit.HOURS)
                    .setConstraints(BatteryNotLowConstraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_CALL_LOG_SYNC,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
                SilaLogger.i("WorkSchedulerManager", "Enqueued periodic call log sync")
            } else {
                val request = OneTimeWorkRequestBuilder<CallLogSyncWorker>()
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                    .build()

                workManager.enqueueUniqueWork(
                    "sila_immediate_call_log_sync",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
                SilaLogger.i("WorkSchedulerManager", "Triggered immediate call log sync")
            }
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to schedule call log sync", e)
        }
    }

    /**
     * Schedules weekly automatic snapshot backup with storage and battery constraints.
     */
    fun scheduleAutoBackup(periodic: Boolean = true) {
        try {
            val constraints = Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true)
                .build()

            if (periodic) {
                val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_AUTO_BACKUP,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
                SilaLogger.i("WorkSchedulerManager", "Enqueued periodic auto-backup")
            } else {
                val request = OneTimeWorkRequestBuilder<AutoBackupWorker>()
                    .setConstraints(constraints)
                    .build()

                workManager.enqueueUniqueWork(
                    "sila_immediate_auto_backup",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
                SilaLogger.i("WorkSchedulerManager", "Triggered immediate auto-backup")
            }
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to schedule auto-backup", e)
        }
    }

    /**
     * Schedules usage and milestone encouragement notifications weekly.
     */
    fun scheduleUsageNotifications() {
        try {
            val request = PeriodicWorkRequestBuilder<UsageNotificationWorker>(7, TimeUnit.DAYS)
                .setConstraints(BatteryNotLowConstraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_USAGE_NOTIFICATIONS,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            SilaLogger.i("WorkSchedulerManager", "Enqueued usage notifications")
        } catch (e: Exception) {
            SilaLogger.e("WorkSchedulerManager", "Failed to schedule usage notifications", e)
        }
    }
}

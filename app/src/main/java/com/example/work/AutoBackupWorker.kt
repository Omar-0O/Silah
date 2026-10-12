package com.example.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.core.util.SilaLogger
import com.example.domain.usecase.BackupRestoreUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker to automatically generate and maintain an on-device local backup snapshot.
 * Injected with [BackupRestoreUseCase] via Hilt.
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRestoreUseCase: BackupRestoreUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                SilaLogger.i("AutoBackupWorker", "Starting scheduled background auto-backup...")
                val result = backupRestoreUseCase.performAutoBackup(applicationContext)

                if (result.success) {
                    SilaLogger.i(
                        "AutoBackupWorker",
                        "Auto-backup succeeded: ${result.relativesCount} relatives, ${result.logsCount} logs."
                    )
                    Result.success(
                        workDataOf(
                            "relatives_count" to result.relativesCount,
                            "logs_count" to result.logsCount
                        )
                    )
                } else {
                    SilaLogger.w("AutoBackupWorker", "Auto-backup failed: ${result.message}")
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            } catch (e: Exception) {
                SilaLogger.e("AutoBackupWorker", "Unexpected error during background auto-backup", e)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}

package com.example.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.core.util.SilaLogger
import com.example.data.RelativeRepository
import com.example.domain.usecase.SyncCallLogsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker to automatically synchronize phone call logs with kinship communication logs.
 * Injected with [SyncCallLogsUseCase] and [RelativeRepository] via Hilt.
 */
@HiltWorker
class CallLogSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncCallLogsUseCase: SyncCallLogsUseCase,
    private val relativeRepository: RelativeRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                SilaLogger.i("CallLogSyncWorker", "Starting background call log sync...")
                val relatives = relativeRepository.getAllRelativesOnce()
                if (relatives.isEmpty()) {
                    SilaLogger.i("CallLogSyncWorker", "No relatives registered. Skipping sync.")
                    return@withContext Result.success()
                }

                val syncedCount = syncCallLogsUseCase(applicationContext, relatives)
                SilaLogger.i("CallLogSyncWorker", "Call log sync finished successfully. Synced $syncedCount calls.")
                Result.success(workDataOf("synced_count" to syncedCount))
            } catch (e: Exception) {
                SilaLogger.e("CallLogSyncWorker", "Error while syncing call logs in background", e)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}

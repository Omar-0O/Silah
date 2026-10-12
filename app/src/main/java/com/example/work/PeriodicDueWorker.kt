package com.example.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.datastore.UserPreferencesRepository
import com.example.core.util.SilaLogger
import com.example.data.RelativeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that runs periodically and on demand
 * to notify the user of any relatives due for contact today.
 * Injected with [RelativeRepository] and [UserPreferencesRepository] via Hilt.
 */
@HiltWorker
class PeriodicDueWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val relativeRepository: RelativeRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val userPrefs = userPreferencesRepository.getInitialPreferences()
                if (!userPrefs.prefNotifyDueRelatives) {
                    return@withContext Result.success()
                }

                val relatives = relativeRepository.getAllRelativesOnce()
                val userName = userPrefs.userName
                val lang = userPrefs.selectedLanguage
                val now = System.currentTimeMillis()

                for (relative in relatives) {
                    val lastContact = relative.lastContactDate
                    val intervalDays = relative.contactIntervalDays

                    val isDue = if (lastContact == 0L) {
                        true // Never called -> Due immediately
                    } else {
                        val diffMs = now - lastContact
                        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                        diffDays >= intervalDays
                    }

                    if (isDue && !ReminderScheduler.wasNotifiedToday(applicationContext, relative.id, now)) {
                        SilaNotificationHelper.sendKinshipNotification(
                            context = applicationContext,
                            relative = relative,
                            lang = lang,
                            userName = userName
                        )

                        ReminderScheduler.markNotifiedToday(applicationContext, relative.id, now)
                    }
                }

                Result.success()
            } catch (e: Exception) {
                SilaLogger.e("PeriodicDueWorker", "Error in periodic due worker", e)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
    }
}

package com.example.domain.usecase

import android.content.Context
import com.example.data.Relative
import com.example.work.ReminderScheduler
import com.example.work.WorkSchedulerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ManageRemindersUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val workSchedulerManager: WorkSchedulerManager
) {

    constructor(context: Context) : this(
        context = context,
        workSchedulerManager = WorkSchedulerManager(context)
    )

    fun scheduleReminderForRelative(relative: Relative) {
        workSchedulerManager.scheduleRelativeReminder(relative)
        ReminderScheduler.scheduleDailyReminder(context)
    }

    fun cancelReminderForRelative(relativeId: Int) {
        workSchedulerManager.cancelRelativeReminder(relativeId)
    }

    fun setupDueNotificationsWorker() {
        workSchedulerManager.schedulePeriodicDueCheck()
        workSchedulerManager.triggerImmediateDueCheck()
    }
}

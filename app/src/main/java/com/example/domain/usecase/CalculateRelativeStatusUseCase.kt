package com.example.domain.usecase

import com.example.data.Relative
import com.example.data.model.RelativeStatus
import javax.inject.Inject

class CalculateRelativeStatusUseCase @Inject constructor() {

    operator fun invoke(relative: Relative): RelativeStatus {
        if (relative.lastContactDate == 0L) {
            return RelativeStatus.NEEDS_CONTACT_URGENT
        }
        val diffMs = System.currentTimeMillis() - relative.lastContactDate
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        val interval = relative.contactIntervalDays

        return when {
            diffDays >= interval + 7 -> RelativeStatus.OVERDUE_CRITICAL
            diffDays >= interval -> RelativeStatus.NEEDS_CONTACT
            diffDays >= interval / 2 -> RelativeStatus.OK_SOON
            else -> RelativeStatus.CONNECTED
        }
    }
}

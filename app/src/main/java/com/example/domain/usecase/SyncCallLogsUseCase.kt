package com.example.domain.usecase

import android.content.Context
import com.example.data.CallLogManager
import com.example.data.CallType
import com.example.data.CommunicationLog
import com.example.data.Relative
import com.example.data.RelativeRepository
import javax.inject.Inject

class SyncCallLogsUseCase @Inject constructor(
    private val repository: RelativeRepository
) {

    suspend operator fun invoke(context: Context, relatives: List<Relative>): Int {
        var syncedCount = 0
        val recentCalls = CallLogManager.fetchRecentCallLogs(context, limit = 300)

        for (relative in relatives) {
            val relativeNormalizedPhone = CallLogManager.normalizePhoneNumber(relative.phone)
            if (relativeNormalizedPhone.isEmpty()) continue

            val latestMatchingCall = recentCalls.firstOrNull { call ->
                CallLogManager.normalizePhoneNumber(call.number) == relativeNormalizedPhone
            }

            if (latestMatchingCall != null && latestMatchingCall.dateTimestamp > relative.lastContactDate) {
                val typeString = when (latestMatchingCall.type) {
                    CallType.INCOMING -> "مكالمة واردة (تلقائي)"
                    CallType.OUTGOING -> "مكالمة صادرة (تلقائي)"
                    CallType.MISSED -> "مكالمة مفقودة"
                    else -> "مكالمة"
                }
                val durationMinutes = latestMatchingCall.durationSeconds / 60
                val notes = "تم رصد $typeString تلقائياً عبر سجل الهاتف (المدة: $durationMinutes دقيقة)"

                val log = CommunicationLog(
                    relativeId = relative.id,
                    type = typeString,
                    notes = notes,
                    timestamp = latestMatchingCall.dateTimestamp
                )
                repository.insertLog(log)
                syncedCount++
            }
        }
        return syncedCount
    }
}

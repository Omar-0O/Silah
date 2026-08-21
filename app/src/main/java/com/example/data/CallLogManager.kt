package com.example.data

import android.content.Context
import android.provider.CallLog

data class CallRecord(
    val number: String,
    val type: CallType,
    val dateTimestamp: Long,
    val durationSeconds: Long
)

enum class CallType(val label: String, val iconRes: String) {
    INCOMING("مكالمة واردة 📲", "incoming"),
    OUTGOING("مكالمة صادرة 📞", "outgoing"),
    MISSED("مكالمة مفقودة ❌", "missed"),
    UNKNOWN("تواصل", "unknown")
}

object CallLogManager {

    /**
     * Cleans and normalizes phone numbers for accurate comparison
     */
    fun normalizePhoneNumber(phone: String): String {
        val digitsOnly = phone.replace("[^0-9+]".toRegex(), "")
        return if (digitsOnly.length > 8) {
            digitsOnly.takeLast(9) // Match last 9 digits to handle country code variations
        } else {
            digitsOnly
        }
    }

    /**
     * Reads all recent call logs from the device ContentResolver safely.
     */
    fun fetchRecentCallLogs(context: Context, limit: Int = 100): List<CallRecord> {
        val records = mutableListOf<CallRecord>()
        try {
            val contentResolver = context.contentResolver
            val uri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )
            val sortOrder = "${CallLog.Calls.DATE} DESC"

            contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)

                var count = 0
                while (cursor.moveToNext() && count < limit) {
                    if (numberIndex != -1 && typeIndex != -1 && dateIndex != -1) {
                        val number = cursor.getString(numberIndex) ?: ""
                        val typeInt = cursor.getInt(typeIndex)
                        val date = cursor.getLong(dateIndex)
                        val duration = if (durationIndex != -1) cursor.getLong(durationIndex) else 0L

                        val callType = when (typeInt) {
                            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                            CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                            else -> CallType.UNKNOWN
                        }

                        if (number.isNotEmpty()) {
                            records.add(CallRecord(number, callType, date, duration))
                            count++
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return records
    }

    /**
     * Finds the latest call log for a specific target phone number.
     */
    fun findLatestCallForPhone(context: Context, rawPhone: String): CallRecord? {
        val targetNormalized = normalizePhoneNumber(rawPhone)
        if (targetNormalized.isEmpty()) return null

        val recentLogs = fetchRecentCallLogs(context, limit = 200)
        return recentLogs.firstOrNull { record ->
            val recordNormalized = normalizePhoneNumber(record.number)
            recordNormalized == targetNormalized
        }
    }
}

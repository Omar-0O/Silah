package com.example.utils

import kotlin.math.abs

object DateUtils {

    /**
     * Formats a timestamp into a friendly, precise Arabic relative time string
     * e.g., "منذ 3 ساعات", "منذ ساعتين", "منذ 15 دقيقة", "أمس", "منذ 4 أيام"
     */
    fun formatRelativeTimeExact(timestamp: Long): String {
        if (timestamp <= 0L) return "لم يتم بعد 🌸"

        val now = System.currentTimeMillis()
        val diffMs = now - timestamp

        if (diffMs < 0) return "اليوم"

        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "منذ لحظات ⚡"
            minutes < 60 -> when (minutes) {
                1L -> "منذ دقيقة واحدة ⏱️"
                2L -> "منذ دقيقتين ⏱️"
                in 3..10 -> "منذ $minutes دقائق ⏱️"
                else -> "منذ $minutes دقيقة ⏱️"
            }
            hours < 24 -> when (hours) {
                1L -> "منذ ساعة واحدة ⏱️"
                2L -> "منذ ساعتين ⏱️"
                in 3..10 -> "منذ $hours ساعات ⏱️"
                else -> "منذ $hours ساعة ⏱️"
            }
            days < 7 -> when (days) {
                1L -> "أمس (منذ 24 ساعة)"
                2L -> "منذ يومين"
                in 3..10 -> "منذ $days أيام"
                else -> "منذ $days يوماً"
            }
            weeks < 4 -> when (weeks) {
                1L -> "منذ أسبوع"
                2L -> "منذ أسبوعين"
                in 3..10 -> "منذ $weeks أسابيع"
                else -> "منذ $weeks أسبوعاً"
            }
            months < 12 -> when (months) {
                1L -> "منذ شهر"
                2L -> "منذ شهرين"
                in 3..10 -> "منذ $months أشهر"
                else -> "منذ $months شهراً"
            }
            else -> when (years) {
                1L -> "منذ سنة"
                2L -> "منذ سنتين"
                in 3..10 -> "منذ $years سنوات"
                else -> "منذ أكثر من سنة"
            }
        }
    }
}

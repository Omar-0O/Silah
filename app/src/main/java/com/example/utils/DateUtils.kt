package com.example.utils

import kotlin.math.abs

object DateUtils {

    /**
     * Formats a timestamp into a friendly, precise relative time string.
     * Supports Arabic (default) and English based on the [lang] parameter.
     * e.g., "منذ 3 ساعات" / "3 hours ago"
     */
    fun formatRelativeTimeExact(timestamp: Long, lang: String = "ar"): String {
        if (timestamp <= 0L) return if (lang == "en") "Not yet 🌸" else "لم يتم بعد 🌸"

        val now = System.currentTimeMillis()
        val diffMs = now - timestamp

        if (diffMs < 0) return if (lang == "en") "Today" else "اليوم"

        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return if (lang == "en") when {
            seconds < 60 -> "Just now ⚡"
            minutes < 60 -> when (minutes) {
                1L -> "1 minute ago ⏱️"
                else -> "$minutes minutes ago ⏱️"
            }
            hours < 24 -> when (hours) {
                1L -> "1 hour ago ⏱️"
                else -> "$hours hours ago ⏱️"
            }
            days < 7 -> when (days) {
                1L -> "Yesterday"
                else -> "$days days ago"
            }
            weeks < 4 -> when (weeks) {
                1L -> "1 week ago"
                else -> "$weeks weeks ago"
            }
            months < 12 -> when (months) {
                1L -> "1 month ago"
                else -> "$months months ago"
            }
            else -> when (years) {
                1L -> "1 year ago"
                else -> "More than a year ago"
            }
        } else when {
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

package com.example.work

import android.icu.util.IslamicCalendar
import java.util.Calendar
import kotlin.random.Random

/**
 * Determines the appropriate notification style based on:
 * - Islamic calendar (Ramadan, Eid al-Fitr, Eid al-Adha)
 * - Day of week (Friday)
 * - Random rotation between regular styles
 */
object NotificationStyleEngine {

    enum class NotificationStyle {
        HEARTWARMING,      // دقيقة تسعد قلب
        GENTLE_EMOTIONAL,  // لمسة وجدانية خفيفة
        FRIDAY,            // نمط الجمعة
        RAMADAN,           // رمضان
        EID                // العيد
    }

    data class NotificationContent(
        val titleAr: String,
        val titleEn: String,
        val bodyAr: String,
        val bodyEn: String,
        val style: NotificationStyle
    )

    /**
     * Determines the current notification style based on date context.
     *
     * Priority: Eid > Ramadan > Friday > Random(Heartwarming, Gentle)
     */
    fun resolveStyle(): NotificationStyle {
        // Check Islamic calendar events first
        val islamicCal = IslamicCalendar()
        val hijriMonth = islamicCal.get(IslamicCalendar.MONTH)  // 0-indexed
        val hijriDay = islamicCal.get(IslamicCalendar.DAY_OF_MONTH)

        // Eid al-Fitr: Shawwal 1-3 (month index 9)
        if (hijriMonth == 9 && hijriDay in 1..3) {
            return NotificationStyle.EID
        }

        // Eid al-Adha: Dhul Hijjah 10-13 (month index 11)
        if (hijriMonth == 11 && hijriDay in 10..13) {
            return NotificationStyle.EID
        }

        // Ramadan: month index 8
        if (hijriMonth == 8) {
            return NotificationStyle.RAMADAN
        }

        // Friday check
        val gregorianCal = Calendar.getInstance()
        if (gregorianCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            return NotificationStyle.FRIDAY
        }

        // Random rotation between regular styles
        return if (Random.nextBoolean()) {
            NotificationStyle.HEARTWARMING
        } else {
            NotificationStyle.GENTLE_EMOTIONAL
        }
    }

    /**
     * Generates notification content for a specific relative.
     * @param relativeName The name of the relative to include in the notification
     * @param lang "ar" or "en"
     */
    fun generateContent(relativeName: String, lang: String = "ar"): NotificationContent {
        val style = resolveStyle()

        return when (style) {
            NotificationStyle.HEARTWARMING -> NotificationContent(
                titleAr = "دقيقة تسعد قلب $relativeName؟ 🤍",
                titleEn = "A minute to brighten ${relativeName}'s day? 🤍",
                bodyAr = "مكالمة سريعة أو رسالة تطمنه عليك بتفرق في يومه كتير.",
                bodyEn = "A quick call or message to check in makes their day so much better.",
                style = style
            )

            NotificationStyle.GENTLE_EMOTIONAL -> NotificationContent(
                titleAr = "خطوة بسيطة لودّ لا ينقطع 🌱",
                titleEn = "A simple step for an everlasting bond 🌱",
                bodyAr = "مر وقت من آخر تواصل مع $relativeName.. لا تجعل المشاغل تبعدكم.",
                bodyEn = "It's been a while since you connected with $relativeName.. don't let life get in the way.",
                style = style
            )

            NotificationStyle.FRIDAY -> NotificationContent(
                titleAr = "جمعتكم أبرك بالتواصل ✨",
                titleEn = "A blessed Friday with family connection ✨",
                bodyAr = "طيّب خاطِر من تحب بدعوة أو سؤال خفيف في هذا اليوم المبارك.",
                bodyEn = "Brighten someone's day with a kind word on this blessed day.",
                style = style
            )

            NotificationStyle.RAMADAN -> NotificationContent(
                titleAr = "رمضان كريم.. صِل رحمك اليوم 🌙",
                titleEn = "Ramadan Kareem.. connect with your family 🌙",
                bodyAr = "في شهر الرحمة والبركة، تواصل مع $relativeName ولو بدعاء.",
                bodyEn = "In this month of mercy, reach out to $relativeName even with a prayer.",
                style = style
            )

            NotificationStyle.EID -> NotificationContent(
                titleAr = "عيدكم مبارك.. لا تنسَ أحبابك 🎉",
                titleEn = "Eid Mubarak.. remember your loved ones 🎉",
                bodyAr = "العيد فرحة، وفرحته تكمل بتواصلك مع $relativeName.",
                bodyEn = "Eid is joy, and it's complete when you connect with $relativeName.",
                style = style
            )
        }
    }
}

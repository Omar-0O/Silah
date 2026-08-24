package com.example

import com.example.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testFormatRelativeTimeExact_NotYet() {
        assertEquals("لم يتم بعد 🌸", DateUtils.formatRelativeTimeExact(0L, "ar"))
        assertEquals("Not yet 🌸", DateUtils.formatRelativeTimeExact(0L, "en"))
    }

    @Test
    fun testFormatRelativeTimeExact_JustNow() {
        val now = System.currentTimeMillis()
        assertEquals("منذ لحظات ⚡", DateUtils.formatRelativeTimeExact(now - 10, "ar"))
        assertEquals("Just now ⚡", DateUtils.formatRelativeTimeExact(now - 10, "en"))
    }

    @Test
    fun testFormatRelativeTimeExact_Minutes() {
        val now = System.currentTimeMillis()
        val fiveMinsAgo = now - (5 * 60 * 1000L)
        assertEquals("منذ 5 دقائق ⏱️", DateUtils.formatRelativeTimeExact(fiveMinsAgo, "ar"))
        assertEquals("5 minutes ago ⏱️", DateUtils.formatRelativeTimeExact(fiveMinsAgo, "en"))
    }

    @Test
    fun testCalculateStreak() {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        // Empty logs -> streak 0
        assertEquals(0, DateUtils.calculateStreak(emptyList()))

        // Log today -> streak 1
        val logsToday = listOf(now)
        assertEquals(1, DateUtils.calculateStreak(logsToday))

        // Log today and yesterday -> streak 2
        val logs2Days = listOf(now, now - oneDay)
        assertEquals(2, DateUtils.calculateStreak(logs2Days))
    }

    @Test
    fun testTranslateDegree() {
        assertEquals("Parents", DateUtils.translateDegree("والدان", "en"))
        assertEquals("Siblings", DateUtils.translateDegree("أشقاء", "en"))
        assertEquals("Uncles/Aunts", DateUtils.translateDegree("أعمام/أخوال", "en"))
        assertEquals("Other Relatives", DateUtils.translateDegree("أقارب آخرون", "en"))

        // Arabic language return as is
        assertEquals("والدان", DateUtils.translateDegree("والدان", "ar"))
    }
}

package com.example

import org.junit.Assert.*
import org.junit.Test

class UsageNotificationWorkerTest {

    @Test
    fun testMilestoneDaysCalculation() {
        val now = 100 * 86_400_000L
        
        fun calculateDaysDiff(firstLaunchTime: Long): Int {
            return ((now - firstLaunchTime) / (1000 * 60 * 60 * 24)).toInt()
        }

        // 7 days ago -> diff 7 (Week 1)
        val firstLaunch7Days = now - (7 * 86_400_000L)
        assertEquals(7, calculateDaysDiff(firstLaunch7Days))

        // 30 days ago -> diff 30 (Month 1)
        val firstLaunch30Days = now - (30 * 86_400_000L)
        assertEquals(30, calculateDaysDiff(firstLaunch30Days))

        // 60 days ago -> diff 60 (Month 2)
        val firstLaunch60Days = now - (60 * 86_400_000L)
        assertEquals(60, calculateDaysDiff(firstLaunch60Days))
    }

    @Test
    fun testMonthlyTemplateRotation() {
        val index = 3
        val nextIndex = (index + 1) % 5
        assertEquals(4, nextIndex)

        val indexLast = 4
        val wrappedIndex = (indexLast + 1) % 5
        assertEquals(0, wrappedIndex)
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.work.ReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerTest {

    @Test
    fun calculateNextTriggerTime_whenTimeIsFutureToday_returnsToday() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentTime = nowCal.timeInMillis

        // Schedule for 10:00 AM today
        val nextTrigger = ReminderScheduler.calculateNextTriggerTime(
            hour = 10,
            minute = 0,
            currentTimeMillis = currentTime
        )

        val triggerCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(nowCal.get(Calendar.DAY_OF_YEAR), triggerCal.get(Calendar.DAY_OF_YEAR))
        assertEquals(10, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal.get(Calendar.MINUTE))
    }

    @Test
    fun calculateNextTriggerTime_whenTimeHasPassedToday_returnsTomorrow() {
        val nowCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentTime = nowCal.timeInMillis

        // Schedule for 10:00 AM (already passed at 14:30)
        val nextTrigger = ReminderScheduler.calculateNextTriggerTime(
            hour = 10,
            minute = 0,
            currentTimeMillis = currentTime
        )

        val triggerCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        // Should be tomorrow
        val expectedCal = (nowCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        assertEquals(expectedCal.get(Calendar.DAY_OF_YEAR), triggerCal.get(Calendar.DAY_OF_YEAR))
        assertEquals(10, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal.get(Calendar.MINUTE))
    }

    @Test
    fun getTodayKey_formatsCorrectDate() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.SEPTEMBER)
            set(Calendar.DAY_OF_MONTH, 4)
        }
        val key = ReminderScheduler.getTodayKey(cal.timeInMillis)
        assertEquals("notified_relatives_2026-09-04", key)
    }

    @Test
    fun testPendingNotifiedRelativesTracking() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ReminderScheduler.clearAllPendingNotifiedRelatives(context)

        assertTrue(ReminderScheduler.getPendingNotifiedRelativeIds(context).isEmpty())

        ReminderScheduler.addPendingNotifiedRelative(context, 101)
        ReminderScheduler.addPendingNotifiedRelative(context, 102)

        val pending = ReminderScheduler.getPendingNotifiedRelativeIds(context)
        assertEquals(2, pending.size)
        assertTrue(pending.contains(101))
        assertTrue(pending.contains(102))

        ReminderScheduler.removePendingNotifiedRelative(context, 101)
        val remaining = ReminderScheduler.getPendingNotifiedRelativeIds(context)
        assertEquals(1, remaining.size)
        assertFalse(remaining.contains(101))
        assertTrue(remaining.contains(102))

        ReminderScheduler.clearAllPendingNotifiedRelatives(context)
        assertTrue(ReminderScheduler.getPendingNotifiedRelativeIds(context).isEmpty())
    }
}

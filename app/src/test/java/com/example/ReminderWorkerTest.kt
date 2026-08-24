package com.example

import com.example.work.ReminderWorker
import org.junit.Assert.*
import org.junit.Test

class ReminderWorkerTest {

    @Test
    fun testBuildNotificationMessage_Parents() {
        val motherMsg = ReminderWorker.buildNotificationMessage("والدتي الحبيبة", "والدان", "ar")
        assertTrue(motherMsg.contains("والدتك"))

        val fatherMsg = ReminderWorker.buildNotificationMessage("أبي الغالي", "والدان", "ar")
        assertTrue(fatherMsg.contains("والدك"))

        val enMsg = ReminderWorker.buildNotificationMessage("Mother", "والدان", "en")
        assertEquals("It's been a while since you checked on your parents 💚", enMsg)
    }

    @Test
    fun testBuildNotificationMessage_Siblings() {
        val sisterMsg = ReminderWorker.buildNotificationMessage("أختي مريم", "أشقاء", "ar")
        assertTrue(sisterMsg.contains("أختك"))

        val brotherMsg = ReminderWorker.buildNotificationMessage("أخي محمد", "أشقاء", "ar")
        assertTrue(brotherMsg.contains("أخوك"))

        val enMsg = ReminderWorker.buildNotificationMessage("Brother", "أشقاء", "en")
        assertEquals("Time to connect with your siblings 🌸", enMsg)
    }

    @Test
    fun testBuildNotificationMessage_UnclesAndAunts() {
        val maternalAuntMsg = ReminderWorker.buildNotificationMessage("خالة فاطمة", "أعمام/أخوال", "ar")
        assertTrue(maternalAuntMsg.contains("خالتك"))

        val paternalAuntMsg = ReminderWorker.buildNotificationMessage("عمة منيرة", "أعمام/أخوال", "ar")
        assertTrue(paternalAuntMsg.contains("عمتك"))

        val maternalUncleMsg = ReminderWorker.buildNotificationMessage("خال أحمد", "أعمام/أخوال", "ar")
        assertTrue(maternalUncleMsg.contains("خالك"))

        val paternalUncleMsg = ReminderWorker.buildNotificationMessage("عم إبراهيم", "أعمام/أخوال", "ar")
        assertTrue(paternalUncleMsg.contains("عمك"))
    }

    @Test
    fun testBuildNotificationMessage_OtherRelatives() {
        val otherMsg = ReminderWorker.buildNotificationMessage("علي", "أقارب آخرون", "ar")
        assertEquals("بقالك فترة مش بتطمن على علي 🌿", otherMsg)

        val enOtherMsg = ReminderWorker.buildNotificationMessage("Ali", "أقارب آخرون", "en")
        assertEquals("It's time to connect with Ali 🌿", enOtherMsg)
    }
}

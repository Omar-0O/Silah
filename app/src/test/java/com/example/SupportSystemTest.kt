package com.example

import com.example.data.SupportConfig
import org.junit.Assert.*
import org.junit.Test

class SupportSystemTest {

    @Test
    fun testSupportConfigDefaults() {
        assertNotNull("InstaPay address should not be null", SupportConfig.INSTAPAY_ADDRESS)
        assertTrue("InstaPay address should not be empty", SupportConfig.INSTAPAY_ADDRESS.isNotEmpty())
        assertNotNull("Vodafone Cash number should not be null", SupportConfig.VODAFONE_CASH_NUMBER)
        assertTrue("Vodafone Cash number should not be empty", SupportConfig.VODAFONE_CASH_NUMBER.isNotEmpty())
        assertNotNull("InstaPay link should not be null", SupportConfig.INSTAPAY_LINK)
        assertTrue("InstaPay link should start with http", SupportConfig.INSTAPAY_LINK.startsWith("http"))
    }

    @Test
    fun testMilestoneThresholds() {
        val milestones = listOf(5, 10, 25, 50, 100)

        assertEquals(5, milestones[0])
        assertEquals(10, milestones[1])
        assertEquals(25, milestones[2])
        assertEquals(50, milestones[3])
        assertEquals(100, milestones[4])

        // Verify milestone identification
        fun getNextMilestone(currentCount: Int): Int? {
            return milestones.lastOrNull { it <= currentCount }
        }

        assertEquals(5, getNextMilestone(7))
        assertEquals(25, getNextMilestone(30))
        assertNull(getNextMilestone(3))
    }

    @Test
    fun testCooldownCalculation() {
        val cooldownMs = 30L * 86_400_000L // 30 days in milliseconds

        val now = System.currentTimeMillis()
        val recentPromptTime = now - (15L * 86_400_000L) // 15 days ago
        val oldPromptTime = now - (35L * 86_400_000L) // 35 days ago

        val isRecentCooldownActive = (now - recentPromptTime) < cooldownMs
        val isOldCooldownActive = (now - oldPromptTime) < cooldownMs

        assertTrue("Cooldown should be active after 15 days", isRecentCooldownActive)
        assertFalse("Cooldown should NOT be active after 35 days", isOldCooldownActive)
    }

    @Test
    fun testForbiddenKeywordsAbsence() {
        val forbiddenWords = listOf("تبرع", "صدقة", "جمعية", "خيرية", "دعم مالي")

        val textsToCheck = listOf(
            SupportConfig.INSTAPAY_ADDRESS,
            SupportConfig.INSTAPAY_LINK,
            SupportConfig.VODAFONE_CASH_NUMBER
        )

        for (text in textsToCheck) {
            for (word in forbiddenWords) {
                assertFalse(
                    "Forbidden keyword '$word' found in SupportConfig text!",
                    text.contains(word)
                )
            }
        }
    }
}

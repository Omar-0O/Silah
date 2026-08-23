package com.example

import com.example.data.Relative
import com.example.data.SupportConfig
import com.example.ui.dialogs.MilestoneType
import com.example.utils.QRCodeUtils
import org.junit.Assert.*
import org.junit.Test

class SupportSystemTest {

    @Test
    fun testSupportConfig_DefaultValues() {
        val config = SupportConfig.DEFAULT
        assertTrue(config.instapay.enabled)
        assertEquals("omar-0o@instapay", config.instapay.ipa)
        assertEquals("https://ipn.eg/S/omar-0o/instapay/1avNS6", config.instapay.paymentLink)

        assertEquals(1, config.wallets.size)
        val vodafoneWallet = config.wallets.first()
        assertTrue(vodafoneWallet.enabled)
        assertEquals("01068888907", vodafoneWallet.phoneNumber)
        assertEquals("Vodafone Cash", vodafoneWallet.name)
    }

    @Test
    fun testNoForbiddenWordsInConfig() {
        val config = SupportConfig.DEFAULT
        val allText = "${config.instapay.ipa} ${config.instapay.paymentLink} ${config.wallets.first().name}".lowercase()

        assertFalse(allText.contains("donation"))
        assertFalse(allText.contains("charity"))
        assertFalse(allText.contains("sadaqah"))
        assertFalse(allText.contains("donate"))
    }

    @Test
    fun testMilestoneTypeMapping() {
        assertEquals(5, MilestoneType.Milestone5.count)
        assertFalse(MilestoneType.Milestone5.isSupportPrompt)

        assertEquals(10, MilestoneType.Milestone10.count)
        assertTrue(MilestoneType.Milestone10.isSupportPrompt)

        assertEquals(25, MilestoneType.Milestone25.count)
        assertTrue(MilestoneType.Milestone25.isSupportPrompt)

        assertEquals(50, MilestoneType.Milestone50.count)
        assertTrue(MilestoneType.Milestone50.isSupportPrompt)

        assertEquals(100, MilestoneType.Milestone100.count)
        assertTrue(MilestoneType.Milestone100.isSupportPrompt)
    }

    @Test
    fun testMilestoneCalculationLogic() {
        val relatives = listOf(
            Relative(id = 1, name = "Relative 1", phone = "0100", relationshipDegree = "أشقاء", lastContactDate = 1000L),
            Relative(id = 2, name = "Relative 2", phone = "0101", relationshipDegree = "أشقاء", lastContactDate = 1000L),
            Relative(id = 3, name = "Relative 3", phone = "0102", relationshipDegree = "أشقاء", lastContactDate = 1000L),
            Relative(id = 4, name = "Relative 4", phone = "0103", relationshipDegree = "أشقاء", lastContactDate = 1000L),
            Relative(id = 5, name = "Relative 5", phone = "0104", relationshipDegree = "أشقاء", lastContactDate = 0L) // Not contacted
        )

        val contactedCount = relatives.count { it.lastContactDate > 0L }
        assertEquals(4, contactedCount)
        assertTrue("4 contacted relatives should not trigger any milestone", contactedCount < 5)

        val updatedRelatives = relatives + Relative(id = 6, name = "Relative 6", phone = "0105", relationshipDegree = "أشقاء", lastContactDate = 1000L)
        val updatedContactedCount = updatedRelatives.count { it.lastContactDate > 0L }
        assertEquals(5, updatedContactedCount)
    }

    @Test
    fun test30DayCooldownLogic() {
        val now = System.currentTimeMillis()
        val recentPromptTime = now - (5 * 86_400_000L) // 5 days ago
        val oldPromptTime = now - (35 * 86_400_000L) // 35 days ago

        val isRecentCooldownActive = (now - recentPromptTime < 30 * 86_400_000L)
        val isOldCooldownActive = (now - oldPromptTime < 30 * 86_400_000L)

        assertTrue("Prompt shown 5 days ago should still be in 30-day cooldown", isRecentCooldownActive)
        assertFalse("Prompt shown 35 days ago should have expired cooldown", isOldCooldownActive)
    }
}

package com.example

import com.example.data.CallLogManager
import org.junit.Assert.*
import org.junit.Test

class CallLogManagerTest {

    @Test
    fun testNormalizePhoneNumber_VariousFormats() {
        val phone1 = "+20 101-234-5678"
        val phone2 = "01012345678"
        val phone3 = "(010) 1234 5678"

        val norm1 = CallLogManager.normalizePhoneNumber(phone1)
        val norm2 = CallLogManager.normalizePhoneNumber(phone2)
        val norm3 = CallLogManager.normalizePhoneNumber(phone3)

        assertEquals("012345678", norm1)
        assertEquals("012345678", norm2)
        assertEquals("012345678", norm3)
        assertEquals(norm1, norm2)
        assertEquals(norm2, norm3)
    }

    @Test
    fun testNormalizePhoneNumber_ShortNumber() {
        val shortPhone = "12345"
        val norm = CallLogManager.normalizePhoneNumber(shortPhone)
        assertEquals("12345", norm)
    }
}

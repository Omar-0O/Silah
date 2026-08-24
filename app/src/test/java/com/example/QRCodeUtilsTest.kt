package com.example

import com.example.utils.QRCodeUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QRCodeUtilsTest {

    @Test
    fun testGenerateQRCodeBitmap_ValidContent() {
        val qrBitmap = QRCodeUtils.generateQRCodeBitmap("https://ipn.eg/S/omar-0o/instapay/1avNS6", 256, 256)
        assertNotNull("Bitmap should be generated for valid string", qrBitmap)
    }

    @Test
    fun testGenerateQRCodeBitmap_BlankContent() {
        val qrBitmap = QRCodeUtils.generateQRCodeBitmap("   ")
        assertNull("Bitmap should be null for blank content", qrBitmap)
    }
}

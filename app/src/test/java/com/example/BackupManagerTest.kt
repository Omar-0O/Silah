package com.example

import com.example.data.BackupManager
import com.example.data.CommunicationLog
import com.example.data.QuickTemplate
import com.example.data.Relative
import org.junit.Assert.*
import org.junit.Test

class BackupManagerTest {

    @Test
    fun testSuggestedFileName() {
        val fileName = BackupManager.suggestedFileName()
        assertTrue("Filename should start with silah_backup_", fileName.startsWith("silah_backup_"))
        assertTrue("Filename should end with .json", fileName.endsWith(".json"))
    }
}

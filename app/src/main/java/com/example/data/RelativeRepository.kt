package com.example.data

import kotlinx.coroutines.flow.Flow

class RelativeRepository(
    private val relativeDao: RelativeDao,
    private val communicationLogDao: CommunicationLogDao,
    private val quickTemplateDao: QuickTemplateDao,
    private val familyMemoryDao: FamilyMemoryDao
) {
    val allRelatives: Flow<List<Relative>> = relativeDao.getAllRelatives()
    val allLogs: Flow<List<CommunicationLog>> = communicationLogDao.getAllLogs()
    val allTemplates: Flow<List<QuickTemplate>> = quickTemplateDao.getAllTemplates()
    val allMemories: Flow<List<FamilyMemory>> = familyMemoryDao.getAllMemories()

    suspend fun getRelativeById(id: Int): Relative? {
        return relativeDao.getRelativeById(id)
    }

    suspend fun insertRelative(relative: Relative): Long {
        return relativeDao.insertRelative(relative)
    }

    suspend fun updateRelative(relative: Relative) {
        relativeDao.updateRelative(relative)
    }

    suspend fun deleteRelative(relative: Relative) {
        relativeDao.deleteRelative(relative)
        communicationLogDao.deleteLogsForRelative(relative.id)
        familyMemoryDao.deleteMemoriesForRelative(relative.id)
    }

    fun getLogsForRelative(relativeId: Int): Flow<List<CommunicationLog>> {
        return communicationLogDao.getLogsForRelative(relativeId)
    }

    fun getMemoriesForRelative(relativeId: Int): Flow<List<FamilyMemory>> {
        return familyMemoryDao.getMemoriesForRelative(relativeId)
    }

    suspend fun insertMemory(memory: FamilyMemory) {
        familyMemoryDao.insertMemory(memory)
    }

    suspend fun deleteMemory(memory: FamilyMemory) {
        familyMemoryDao.deleteMemory(memory)
    }

    suspend fun insertLog(log: CommunicationLog) {
        communicationLogDao.insertLog(log)
        // Update lastContactDate on the relative
        val relative = relativeDao.getRelativeById(log.relativeId)
        if (relative != null) {
            // Keep the maximum contact timestamp if logs are entered retroactively
            val maxTimestamp = maxOf(relative.lastContactDate, log.timestamp)
            val updated = relative.copy(lastContactDate = maxTimestamp)
            relativeDao.updateRelative(updated)
        }
    }

    // BUG-02 Fix: Check duplicate before inserting auto-synced call log
    suspend fun existsLogAt(relativeId: Int, timestamp: Long): Boolean {
        return communicationLogDao.existsLogAt(relativeId, timestamp) > 0
    }

    suspend fun insertTemplate(template: QuickTemplate) {
        quickTemplateDao.insertTemplate(template)
    }
}

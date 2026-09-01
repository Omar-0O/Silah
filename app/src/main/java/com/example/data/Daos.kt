package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RelativeDao {
    @Query("SELECT * FROM relatives ORDER BY name ASC")
    fun getAllRelatives(): Flow<List<Relative>>

    @Query("SELECT * FROM relatives ORDER BY name ASC")
    suspend fun getAllRelativesOnce(): List<Relative>

    @Query("SELECT * FROM relatives WHERE id = :id")
    suspend fun getRelativeById(id: Int): Relative?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelative(relative: Relative): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelatives(relatives: List<Relative>)

    @Update
    suspend fun updateRelative(relative: Relative)

    @Delete
    suspend fun deleteRelative(relative: Relative)
}

@Dao
interface CommunicationLogDao {
    @Query("SELECT * FROM communication_logs WHERE relativeId = :relativeId ORDER BY timestamp DESC")
    fun getLogsForRelative(relativeId: Int): Flow<List<CommunicationLog>>

    @Query("SELECT * FROM communication_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CommunicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommunicationLog)

    // BUG-02 Fix: Check for duplicate before inserting auto-synced call logs
    @Query("SELECT COUNT(*) FROM communication_logs WHERE relativeId = :relativeId AND timestamp = :timestamp")
    suspend fun existsLogAt(relativeId: Int, timestamp: Long): Int

    @Query("DELETE FROM communication_logs WHERE relativeId = :relativeId")
    suspend fun deleteLogsForRelative(relativeId: Int)
}

@Dao
interface QuickTemplateDao {
    @Query("SELECT * FROM quick_templates")
    fun getAllTemplates(): Flow<List<QuickTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: QuickTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<QuickTemplate>)
}

@Dao
interface FamilyMemoryDao {
    @Query("SELECT * FROM family_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<FamilyMemory>>

    @Query("SELECT * FROM family_memories WHERE relativeId = :relativeId ORDER BY timestamp DESC")
    fun getMemoriesForRelative(relativeId: Int): Flow<List<FamilyMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: FamilyMemory)

    @Delete
    suspend fun deleteMemory(memory: FamilyMemory)

    @Query("DELETE FROM family_memories WHERE relativeId = :relativeId")
    suspend fun deleteMemoriesForRelative(relativeId: Int)
}

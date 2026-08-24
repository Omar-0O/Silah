package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CommunicationLog
import com.example.data.Relative
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndGetRelative() = runBlocking {
        val relative = Relative(
            id = 1,
            name = "أحمد علي",
            phone = "01012345678",
            relationshipDegree = "أشقاء",
            lastContactDate = System.currentTimeMillis(),
            contactIntervalDays = 7,
            notes = "أخي الكبيير"
        )

        database.relativeDao().insertRelative(relative)

        val retrieved = database.relativeDao().getRelativeById(1)
        assertNotNull(retrieved)
        assertEquals("أحمد علي", retrieved?.name)
        assertEquals("01012345678", retrieved?.phone)
        assertEquals("أشقاء", retrieved?.relationshipDegree)
    }

    @Test
    fun testUpdateRelative() = runBlocking {
        val relative = Relative(
            id = 2,
            name = "محمود علي",
            phone = "01100000000",
            relationshipDegree = "والدان",
            lastContactDate = 0L,
            contactIntervalDays = 3
        )
        database.relativeDao().insertRelative(relative)

        val updatedRelative = relative.copy(name = "محمود علي (والدي)", lastContactDate = 10000L)
        database.relativeDao().updateRelative(updatedRelative)

        val result = database.relativeDao().getRelativeById(2)
        assertEquals("محمود علي (والدي)", result?.name)
        assertEquals(10000L, result?.lastContactDate)
    }

    @Test
    fun testDeleteRelative() = runBlocking {
        val relative = Relative(
            id = 3,
            name = "عمر خالد",
            phone = "01200000000",
            relationshipDegree = "أقارب آخرون"
        )
        database.relativeDao().insertRelative(relative)

        database.relativeDao().deleteRelative(relative)
        val result = database.relativeDao().getRelativeById(3)
        assertNull(result)
    }

    @Test
    fun testCommunicationLogs() = runBlocking {
        val relative = Relative(id = 10, name = "سارة", phone = "01500000000", relationshipDegree = "أشقاء")
        database.relativeDao().insertRelative(relative)

        val log1 = CommunicationLog(id = 1, relativeId = 10, type = "اتصال هاتفي", timestamp = 1000L, notes = "اطمأنان")
        val log2 = CommunicationLog(id = 2, relativeId = 10, type = "زيارة", timestamp = 2000L, notes = "زيارة منزلية")

        database.communicationLogDao().insertLog(log1)
        database.communicationLogDao().insertLog(log2)

        val logs = database.communicationLogDao().getLogsForRelative(10).first()
        assertEquals(2, logs.size)
        // Ordered timestamp DESC -> log2 first
        assertEquals("زيارة", logs[0].type)
        assertEquals("اتصال هاتفي", logs[1].type)

        // Delete logs
        database.communicationLogDao().deleteLogsForRelative(10)
        val emptyLogs = database.communicationLogDao().getLogsForRelative(10).first()
        assertTrue(emptyLogs.isEmpty())
    }
}

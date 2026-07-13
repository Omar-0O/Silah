package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Relative::class, CommunicationLog::class, QuickTemplate::class, FamilyMemory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun relativeDao(): RelativeDao
    abstract fun communicationLogDao(): CommunicationLogDao
    abstract fun quickTemplateDao(): QuickTemplateDao
    abstract fun familyMemoryDao(): FamilyMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "silat_rahim_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialTemplates(database.quickTemplateDao())
                    populateInitialRelatives(database.relativeDao())
                }
            }
        }

        suspend fun populateInitialTemplates(dao: QuickTemplateDao) {
            val initialTemplates = listOf(
                QuickTemplate(
                    title = "تهنئة يوم الجمعة",
                    content = "السلام عليكم ورحمة الله وبركاته. طيب الله جمعتكم بكل خير، وجعلها الله مغفرة لذنوبكم وباباً للرزق والبركة. طاب يومكم.",
                    category = "يوم الجمعة"
                ),
                QuickTemplate(
                    title = "سؤال عن الحال",
                    content = "السلام عليكم يا غالي، أردت فقط الاطمئنان على صحتك وأحوالك. أسأل الله أن تكون دائماً في أتم الصحة والعافية. مشتاقون لرؤيتك قريبًا.",
                    category = "سؤال عام"
                ),
                QuickTemplate(
                    title = "تهنئة بالعيد",
                    content = "كل عام وأنتم بخير وصحة وعافية! بمناسبة حلول العيد المبارك، أعاده الله علينا وعليكم باليمن والبركات، وتقبل الله منا ومنكم صالح الأعمال.",
                    category = "أعياد ومناسبات"
                ),
                QuickTemplate(
                    title = "دعاء بالشفاء",
                    content = "أسأل الله العظيم رب العرش العظيم أن يشفيك شفاءً لا يغادر سقماً، ويلبسك ثوب الصحة والعافية ويحفظك لنا من كل سوء.",
                    category = "دعاء وعيادة"
                ),
                QuickTemplate(
                    title = "شكر وتقدير",
                    content = "أتقدم إليكم بخالص الشكر والتقدير والمحبة على طيب تواصلكم ولطفكم، دمتم لي سنداً وذخراً في هذه الحياة.",
                    category = "شكر"
                )
            )
            dao.insertTemplates(initialTemplates)
        }

        suspend fun populateInitialRelatives(dao: RelativeDao) {
            // Seed a couple of default relatives to ensure the list is not blank on fresh install
            val initialRelatives = listOf(
                Relative(
                    name = "الوالد الغالي",
                    phone = "+966500000001",
                    relationshipDegree = "والدان",
                    contactIntervalDays = 1,
                    notes = "باب الجنة الأوسط، احرص على الاتصال به أو زيارته يومياً"
                ),
                Relative(
                    name = "الوالدة العزيزة",
                    phone = "+966500000002",
                    relationshipDegree = "والدان",
                    contactIntervalDays = 1,
                    notes = "الجنة تحت أقدامها، اتصل بها صباحاً ومساءً"
                )
            )
            for (relative in initialRelatives) {
                dao.insertRelative(relative)
            }
        }
    }
}

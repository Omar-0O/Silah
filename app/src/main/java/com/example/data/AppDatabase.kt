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
    version = 4,
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

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "silat_rahim_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(AppDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            try {
                db.execSQL(
                    "INSERT INTO quick_templates (title, content, category) VALUES " +
                    "('تهنئة يوم الجمعة', 'السلام عليكم ورحمة الله وبركاته. طيب الله جمعتكم بكل خير، وجعلها الله مغفرة لذنوبكم وباباً للرزق والبركة. طاب يومكم.', 'يوم الجمعة'), " +
                    "('سؤال عن الحال', 'السلام عليكم يا غالي، أردت فقط الاطمئنان على صحتك وأحوالك. أسأل الله أن تكون دائماً في أتم الصحة والعافية. مشتاقون لرؤيتك قريبًا.', 'سؤال عام'), " +
                    "('تهنئة بالعيد', 'كل عام وأنتم بخير وصحة وعافية! بمناسبة حلول العيد المبارك، أعاده الله علينا وعليكم باليمن والبركات، وتقبل الله منا ومنكم صالح الأعمال.', 'أعياد ومناسبات'), " +
                    "('دعاء بالشفاء', 'أسأل الله العظيم رب العرش العظيم أن يشفيك شفاءً لا يغادر سقماً، ويلبسك ثوب الصحة والعافية ويحفظك لنا من كل سوء.', 'دعاء وعيادة'), " +
                    "('شكر وتقدير', 'أتقدم إليكم بخالص الشكر والتقدير والمحبة على طيب تواصلكم ولطفكم، دمتم لي سنداً وذخراً في هذه الحياة.', 'شكر')"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

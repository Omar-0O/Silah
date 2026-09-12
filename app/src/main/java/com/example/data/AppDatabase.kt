package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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

        // ─────────────────────────────────────────────────────────────────────
        // Migrations — preserve user data across version upgrades
        // ─────────────────────────────────────────────────────────────────────

        /** v1 → v2: Added quick_templates table */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `quick_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `category` TEXT NOT NULL
                    )"""
                )
                // Seed default templates
                seedDefaultTemplates(db)
            }
        }

        /** v2 → v3: Added family_memories table */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `family_memories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `relativeId` INTEGER NOT NULL,
                        `relativeName` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `imagePath` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )"""
                )
            }
        }

        /** v3 → v4: Added photoUri column to relatives */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add photoUri column with NULL default (safe for existing rows)
                db.execSQL("ALTER TABLE `relatives` ADD COLUMN `photoUri` TEXT DEFAULT NULL")
            }
        }

        /** Skip migrations for users jumping from v1 directly to v4 */
        private val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        private val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "silat_rahim_database"
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_1_3,
                    MIGRATION_1_4,
                    MIGRATION_2_4
                )
                // Last-resort fallback: only if a migration path is truly missing
                // (e.g., downgrade from a future version). This should never trigger
                // for normal upgrades now that all migrations are defined.
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .addCallback(AppDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedDefaultTemplates(db: SupportSQLiteDatabase) {
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

    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedDefaultTemplates(db)
        }
    }
}

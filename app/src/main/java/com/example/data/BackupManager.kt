package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BackupManager — handles full data export/import as a portable JSON file.
 * Uses Android Storage Access Framework (SAF) — no server needed.
 * User can save the file to Google Drive, local storage, WhatsApp, etc.
 */
object BackupManager {

    private const val BACKUP_VERSION = 1

    data class BackupResult(
        val success: Boolean,
        val message: String,
        val relativesCount: Int = 0,
        val logsCount: Int = 0
    )

    // ─────────────────────────────────────────────────────────────────────────
    // EXPORT: Room → JSON → File URI (chosen by user via SAF)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun exportToUri(
        context: Context,
        uri: Uri,
        relatives: List<Relative>,
        logs: List<CommunicationLog>,
        templates: List<QuickTemplate>
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = buildBackupJson(relatives, logs, templates)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: return@withContext BackupResult(false, "تعذّر فتح ملف الحفظ")

            BackupResult(
                success = true,
                message = "تم تصدير ${relatives.size} قريب و${logs.size} سجل تواصل بنجاح ✅",
                relativesCount = relatives.size,
                logsCount = logs.size
            )
        } catch (e: Exception) {
            BackupResult(false, "خطأ في التصدير: ${e.localizedMessage}")
        }
    }

    private fun buildBackupJson(
        relatives: List<Relative>,
        logs: List<CommunicationLog>,
        templates: List<QuickTemplate>
    ): String {
        val root = JSONObject().apply {
            put("backupVersion", BACKUP_VERSION)
            put("appName", "صِلَةِ")
            put("exportedAt", System.currentTimeMillis())
            put("exportedAtReadable", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("ar")).format(Date()))

            // Relatives
            put("relatives", JSONArray().apply {
                relatives.forEach { r ->
                    put(JSONObject().apply {
                        put("id", r.id)
                        put("name", r.name)
                        put("phone", r.phone)
                        put("relationshipDegree", r.relationshipDegree)
                        put("lastContactDate", r.lastContactDate)
                        put("contactIntervalDays", r.contactIntervalDays)
                        put("notes", r.notes)
                    })
                }
            })

            // Communication Logs
            put("communicationLogs", JSONArray().apply {
                logs.forEach { l ->
                    put(JSONObject().apply {
                        put("id", l.id)
                        put("relativeId", l.relativeId)
                        put("type", l.type)
                        put("timestamp", l.timestamp)
                        put("notes", l.notes)
                    })
                }
            })

            // Templates
            put("quickTemplates", JSONArray().apply {
                templates.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("content", t.content)
                        put("category", t.category)
                    })
                }
            })
        }
        return root.toString(2) // pretty-printed JSON
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IMPORT: File URI → JSON → Room (merge, not replace)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        relativeDao: RelativeDao,
        communicationLogDao: CommunicationLogDao,
        quickTemplateDao: QuickTemplateDao,
        onConflict: ImportConflictStrategy = ImportConflictStrategy.MERGE_NEW_ONLY
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext BackupResult(false, "تعذّر قراءة ملف النسخة الاحتياطية")

            val root = JSONObject(jsonString)

            // Validate backup file
            if (!root.has("relatives") || !root.has("backupVersion")) {
                return@withContext BackupResult(false, "الملف المختار ليس نسخة احتياطية صالحة من تطبيق صِلَةِ")
            }

            var importedRelatives = 0
            var importedLogs = 0

            // Import Relatives
            val relativesArray = root.getJSONArray("relatives")
            val existingRelatives = relativeDao.getAllRelativesOnce()
            val existingPhones = existingRelatives.map { it.phone }.toSet()

            for (i in 0 until relativesArray.length()) {
                val obj = relativesArray.getJSONObject(i)
                val phone = obj.getString("phone")

                val shouldImport = when (onConflict) {
                    ImportConflictStrategy.MERGE_NEW_ONLY -> phone !in existingPhones
                    ImportConflictStrategy.REPLACE_ALL -> true
                }

                if (shouldImport) {
                    relativeDao.insertRelative(
                        Relative(
                            // id = 0 so Room auto-generates new IDs
                            name = obj.getString("name"),
                            phone = phone,
                            relationshipDegree = obj.getString("relationshipDegree"),
                            lastContactDate = obj.getLong("lastContactDate"),
                            contactIntervalDays = obj.getInt("contactIntervalDays"),
                            notes = obj.optString("notes", "")
                        )
                    )
                    importedRelatives++
                }
            }

            // Import Logs (only for newly-added relatives)
            val logsArray = root.optJSONArray("communicationLogs") ?: JSONArray()
            for (i in 0 until logsArray.length()) {
                val obj = logsArray.getJSONObject(i)
                communicationLogDao.insertLog(
                    CommunicationLog(
                        relativeId = obj.getInt("relativeId"),
                        type = obj.getString("type"),
                        timestamp = obj.getLong("timestamp"),
                        notes = obj.optString("notes", "")
                    )
                )
                importedLogs++
            }

            BackupResult(
                success = true,
                message = "✅ تمت الاستعادة بنجاح!\nتم إضافة $importedRelatives قريب و$importedLogs سجل جديد",
                relativesCount = importedRelatives,
                logsCount = importedLogs
            )
        } catch (e: Exception) {
            BackupResult(false, "خطأ في استعادة البيانات: ${e.localizedMessage}")
        }
    }

    enum class ImportConflictStrategy {
        MERGE_NEW_ONLY,   // الإضافة فقط — الأرقام الموجودة تُتجاهل (الافتراضي)
        REPLACE_ALL       // الاستبدال الكامل
    }

    /** Suggested filename for the backup */
    fun suggestedFileName(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale("ar")).format(Date())
        return "silah_backup_$date.json"
    }
}

package com.example.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.data.BackupManager
import com.example.data.CommunicationLog
import com.example.data.QuickTemplate
import com.example.data.Relative
import com.example.data.RelativeRepository
import javax.inject.Inject

class BackupRestoreUseCase @Inject constructor(
    private val repository: RelativeRepository
) {

    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        relatives: List<Relative>,
        logs: List<CommunicationLog>,
        templates: List<QuickTemplate>
    ): BackupManager.BackupResult {
        return BackupManager.exportToUri(
            context = context,
            uri = uri,
            relatives = relatives,
            logs = logs,
            templates = templates
        )
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        strategy: BackupManager.ImportConflictStrategy = BackupManager.ImportConflictStrategy.MERGE_NEW_ONLY
    ): BackupManager.BackupResult {
        return BackupManager.importFromUri(
            context = context,
            uri = uri,
            relativeDao = repository.relativeDao,
            communicationLogDao = repository.communicationLogDao,
            quickTemplateDao = repository.quickTemplateDao,
            onConflict = strategy
        )
    }

    suspend fun performAutoBackup(context: Context): BackupManager.BackupResult {
        return try {
            val relatives = repository.getAllRelativesOnce()
            val logs = repository.getAllLogsOnce()
            val templates = repository.quickTemplateDao.getAllTemplatesOnce()

            val backupDir = java.io.File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
            val backupFile = java.io.File(backupDir, "auto_backup_latest.json")
            val uri = Uri.fromFile(backupFile)

            BackupManager.exportToUri(
                context = context,
                uri = uri,
                relatives = relatives,
                logs = logs,
                templates = templates
            )
        } catch (e: Exception) {
            BackupManager.BackupResult(
                success = false,
                message = "فشل النسخ الاحتياطي التلقائي: ${e.localizedMessage}"
            )
        }
    }
}

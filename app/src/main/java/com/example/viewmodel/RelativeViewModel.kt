package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CommunicationLog
import com.example.data.Relative
import com.example.data.RelativeRepository
import com.example.data.QuickTemplate
import com.example.data.FamilyMemory
import android.app.NotificationManager
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.work.PeriodicDueWorker
import com.example.work.ReminderWorker
import com.example.work.UsageNotificationWorker
import java.util.concurrent.TimeUnit
import android.net.Uri
import com.example.data.BackupManager
import com.example.data.CallLogManager
import com.example.data.CallType
import com.example.widget.SilaAppWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RelativeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = RelativeRepository(
        db.relativeDao(),
        db.communicationLogDao(),
        db.quickTemplateDao(),
        db.familyMemoryDao()
    )

    init {
        setupDueNotificationsWorker()
        setupUsageNotificationWorker()
        ensureFirstLaunchRecorded()
        observeMilestones()
    }

    // Raw database state flows
    val relatives: StateFlow<List<Relative>> = repository.allRelatives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CommunicationLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streakDays: StateFlow<Int> = logs
        .combine(relatives) { logsList, _ ->
            com.example.utils.DateUtils.calculateStreak(logsList.map { it.timestamp })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val last7DaysActivity: StateFlow<List<Boolean>> = logs
        .combine(relatives) { logsList, _ ->
            com.example.utils.DateUtils.getLast7DaysActivity(logsList.map { it.timestamp })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { false })

    val templates: StateFlow<List<QuickTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<FamilyMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل") // "الكل", "والدان", "أشقاء", "أعمام/أخوال", "أقارب آخرون"

    // Filtered & Sorted Relatives Flow (Sorted by nearest contact due date / highest urgency)
    val filteredRelatives: StateFlow<List<Relative>> = combine(
        relatives,
        searchQuery,
        selectedCategory
    ) { relativesList, query, category ->
        relativesList.filter { relative ->
            val matchesSearch = relative.name.contains(query, ignoreCase = true) || 
                                relative.phone.contains(query)
            val matchesCategory = category == "الكل" || relative.relationshipDegree == category
            matchesSearch && matchesCategory
        }.sortedWith(
            compareBy<Relative> { relative ->
                if (relative.lastContactDate == 0L) 0L
                else relative.lastContactDate + (relative.contactIntervalDays * 86_400_000L)
            }.thenBy { relative ->
                when (relative.relationshipDegree) {
                    "والدان" -> 1
                    "أشقاء" -> 2
                    "أعمام/أخوال" -> 3
                    else -> 4
                }
            }.thenBy { it.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI triggers/States for Dialogs and Forms
    val showAddRelativeDialog = MutableStateFlow(false)
    val showEditRelativeDialog = MutableStateFlow<Relative?>(null)
    val showSettingsDialog = MutableStateFlow(false)
    val showImportContactsDialog = MutableStateFlow(false)
    val showRecordLogDialog = MutableStateFlow<Relative?>(null)
    val showLogsHistoryDialog = MutableStateFlow<Relative?>(null)
    val showQuickTemplatesDialog = MutableStateFlow<Relative?>(null)
    val showSetReminderDialog = MutableStateFlow<Relative?>(null)
    val showSupportSilaDialog = MutableStateFlow(false)
    val activeMilestone = MutableStateFlow<com.example.ui.dialogs.MilestoneType?>(null)

    // Dark mode state persisted in SharedPreferences
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    // Notification preferences persisted in SharedPreferences
    val prefNotifyDueRelatives = MutableStateFlow(prefs.getBoolean("pref_notify_due_relatives", true))
    val prefNotifyEncouragement = MutableStateFlow(prefs.getBoolean("pref_notify_encouragement", true))
    val prefNotifyMonthly = MutableStateFlow(prefs.getBoolean("pref_notify_monthly", true))

    fun toggleNotifyDueRelatives(enabled: Boolean) {
        prefNotifyDueRelatives.value = enabled
        prefs.edit().putBoolean("pref_notify_due_relatives", enabled).apply()
    }

    fun toggleNotifyEncouragement(enabled: Boolean) {
        prefNotifyEncouragement.value = enabled
        prefs.edit().putBoolean("pref_notify_encouragement", enabled).apply()
    }

    fun toggleNotifyMonthly(enabled: Boolean) {
        prefNotifyMonthly.value = enabled
        prefs.edit().putBoolean("pref_notify_monthly", enabled).apply()
    }

    // Days using Sila
    val appUsageDays: StateFlow<Int> = MutableStateFlow(
        run {
            val firstLaunch = prefs.getLong("app_first_launch_time", System.currentTimeMillis())
            val diffMs = System.currentTimeMillis() - firstLaunch
            ((diffMs / (1000 * 60 * 60 * 24)) + 1).toInt()
        }
    )

    fun openSupportSilaDialog() { showSupportSilaDialog.value = true }
    fun closeSupportSilaDialog() { showSupportSilaDialog.value = false }

    private fun ensureFirstLaunchRecorded() {
        if (!prefs.contains("app_first_launch_time")) {
            prefs.edit().putLong("app_first_launch_time", System.currentTimeMillis()).apply()
        }
    }

    private fun observeMilestones() {
        viewModelScope.launch {
            relatives.collect { rels ->
                checkMilestones(rels)
            }
        }
    }

    fun checkMilestones(currentRelatives: List<Relative>) {
        val contactedCount = currentRelatives.count { it.lastContactDate > 0L }
        if (contactedCount < 5) return

        val achievedSet = prefs.getStringSet("achieved_milestones", emptySet()) ?: emptySet()
        val lastPromptTime = prefs.getLong("last_support_prompt_time", 0L)
        val now = System.currentTimeMillis()
        val isCooldownActive = lastPromptTime > 0L && (now - lastPromptTime < 30 * 86_400_000L)

        when {
            contactedCount >= 100 && !achievedSet.contains("milestone_100") -> {
                if (!isCooldownActive) {
                    activeMilestone.value = com.example.ui.dialogs.MilestoneType.Milestone100
                    markMilestoneAchieved("milestone_100")
                }
            }
            contactedCount >= 50 && !achievedSet.contains("milestone_50") -> {
                if (!isCooldownActive) {
                    activeMilestone.value = com.example.ui.dialogs.MilestoneType.Milestone50
                    markMilestoneAchieved("milestone_50")
                }
            }
            contactedCount >= 25 && !achievedSet.contains("milestone_25") -> {
                if (!isCooldownActive) {
                    activeMilestone.value = com.example.ui.dialogs.MilestoneType.Milestone25
                    markMilestoneAchieved("milestone_25")
                }
            }
            contactedCount >= 10 && !achievedSet.contains("milestone_10") -> {
                if (!isCooldownActive) {
                    activeMilestone.value = com.example.ui.dialogs.MilestoneType.Milestone10
                    markMilestoneAchieved("milestone_10")
                }
            }
            contactedCount >= 5 && !achievedSet.contains("milestone_5") -> {
                activeMilestone.value = com.example.ui.dialogs.MilestoneType.Milestone5
                markMilestoneAchieved("milestone_5")
            }
        }
    }

    private fun markMilestoneAchieved(milestoneId: String) {
        val currentSet = prefs.getStringSet("achieved_milestones", emptySet()) ?: emptySet()
        val updated = currentSet.toMutableSet().apply { add(milestoneId) }
        prefs.edit().putStringSet("achieved_milestones", updated).apply()
    }

    fun onMilestoneNotNow() {
        activeMilestone.value = null
        prefs.edit().putLong("last_support_prompt_time", System.currentTimeMillis()).apply()
    }

    fun onMilestoneSupportClick() {
        activeMilestone.value = null
        prefs.edit().putLong("last_support_prompt_time", System.currentTimeMillis()).apply()
        showSupportSilaDialog.value = true
    }

    fun setupUsageNotificationWorker() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workManager = WorkManager.getInstance(getApplication())
                val usageWork = PeriodicWorkRequestBuilder<UsageNotificationWorker>(24, TimeUnit.HOURS)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    "sila_periodic_usage_check",
                    ExistingPeriodicWorkPolicy.KEEP,
                    usageWork
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // App language state persisted in SharedPreferences ("ar" or "en")
    val selectedLanguage = MutableStateFlow(prefs.getString("selected_language", "ar") ?: "ar")

    fun selectLanguage(langCode: String) {
        selectedLanguage.value = langCode
        prefs.edit().putString("selected_language", langCode).apply()
    }

    // User Profile state (Name, Gender & Avatar)
    val userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userGender = MutableStateFlow(prefs.getString("user_gender", "male") ?: "male")
    val userAvatarId = MutableStateFlow(prefs.getString("user_avatar_id", "avatar_01") ?: "avatar_01")

    fun saveUserProfile(name: String, gender: String) {
        userName.value = name
        userGender.value = gender
        prefs.edit()
            .putString("user_name", name)
            .putString("user_gender", gender)
            .apply()
    }

    fun saveUserAvatar(avatarId: String) {
        userAvatarId.value = avatarId
        prefs.edit().putString("user_avatar_id", avatarId).apply()
    }

    // ─────────────────────────────────────────────────────────────────────
    // Backup & Restore
    // ─────────────────────────────────────────────────────────────────────
    val backupResult = MutableStateFlow<BackupManager.BackupResult?>(null)

    // Launchers are set from MainActivity (Compose context)
    private var exportLauncher: (() -> Unit)? = null
    private var importLauncher: (() -> Unit)? = null

    fun setExportLauncher(launcher: () -> Unit) { exportLauncher = launcher }
    fun setImportLauncher(launcher: () -> Unit) { importLauncher = launcher }

    fun triggerExport() { exportLauncher?.invoke() }
    fun triggerImport() { importLauncher?.invoke() }
    fun suggestedBackupName() = BackupManager.suggestedFileName()

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = BackupManager.exportToUri(
                context = context,
                uri = uri,
                relatives = relatives.value,
                logs = logs.value,
                templates = templates.value
            )
            backupResult.value = result
        }
    }

    fun importBackup(
        context: Context,
        uri: Uri,
        strategy: BackupManager.ImportConflictStrategy = BackupManager.ImportConflictStrategy.MERGE_NEW_ONLY
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = BackupManager.importFromUri(
                context = context,
                uri = uri,
                relativeDao = db.relativeDao(),
                communicationLogDao = db.communicationLogDao(),
                quickTemplateDao = db.quickTemplateDao(),
                onConflict = strategy
            )
            backupResult.value = result
            if (result.success) {
                SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
            }
        }
    }

    fun clearBackupResult() { backupResult.value = null }

    // Call Log Sync State
    val isSyncingCallLogs = MutableStateFlow(false)

    /**
     * Automatic Call Log Sync: Matches call logs with relatives & updates lastContactDate
     */
    fun syncCallLogsWithRelatives(context: Context, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            isSyncingCallLogs.value = true
            var syncedCount = 0
            try {
                val recentCalls = CallLogManager.fetchRecentCallLogs(context, limit = 300)
                val currentRelatives = relatives.value

                for (relative in currentRelatives) {
                    val relativeNormalizedPhone = CallLogManager.normalizePhoneNumber(relative.phone)
                    if (relativeNormalizedPhone.isEmpty()) continue

                    // Find latest call matching this relative
                    val latestMatchingCall = recentCalls.firstOrNull { call ->
                        CallLogManager.normalizePhoneNumber(call.number) == relativeNormalizedPhone
                    }

                    if (latestMatchingCall != null && latestMatchingCall.dateTimestamp > relative.lastContactDate) {
                        // Record new communication log automatically
                        val typeString = when (latestMatchingCall.type) {
                            CallType.INCOMING -> "مكالمة واردة (تلقائي)"
                            CallType.OUTGOING -> "مكالمة صادرة (تلقائي)"
                            CallType.MISSED -> "مكالمة مفقودة"
                            else -> "مكالمة"
                        }
                        val durationMinutes = latestMatchingCall.durationSeconds / 60
                        val notes = "تم رصد $typeString تلقائياً عبر سجل الهاتف (المدة: $durationMinutes دقيقة)"

                        val log = CommunicationLog(
                            relativeId = relative.id,
                            type = typeString,
                            notes = notes,
                            timestamp = latestMatchingCall.dateTimestamp
                        )
                        repository.insertLog(log)
                        syncedCount++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncingCallLogs.value = false
                if (syncedCount > 0) {
                    SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
                }
                onComplete?.invoke(syncedCount)
            }
        }
    }

data class DeviceContact(
    val name: String,
    val phone: String,
    val isGoogle: Boolean = false,
    val photoUri: String? = null
)

    // Contact importing state
    val deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val isLoadingContacts = MutableStateFlow(false)

    // Load actual device contacts using ContentResolver
    fun fetchDeviceContacts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingContacts.value = true
            val contactsList = mutableListOf<DeviceContact>()
            try {
                val contentResolver = context.contentResolver
                val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val projection = arrayOf(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
                    android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    "account_type"
                )
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoUriIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    val accountTypeIndex = cursor.getColumnIndex("account_type")
                    while (cursor.moveToNext()) {
                        if (nameIndex != -1 && numberIndex != -1) {
                            val name = cursor.getString(nameIndex) ?: ""
                            val number = cursor.getString(numberIndex) ?: ""
                            val photoUri = if (photoUriIndex != -1) cursor.getString(photoUriIndex) else null
                            val accountType = if (accountTypeIndex != -1) cursor.getString(accountTypeIndex) else null
                            val isGoogle = accountType?.contains("google", ignoreCase = true) == true
                            if (name.isNotEmpty() && number.isNotEmpty()) {
                                contactsList.add(DeviceContact(name, number, isGoogle, photoUri))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Sort, remove duplicates, and update state
            deviceContacts.value = contactsList
                .distinctBy { it.phone.replace("\\s|-|\\(|\\)".toRegex(), "") }
                .sortedBy { it.name }
            isLoadingContacts.value = false
        }
    }

    // Insert functions
    fun addRelative(
        name: String,
        phone: String,
        relationshipDegree: String,
        intervalDays: Int,
        notes: String,
        photoUri: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val relative = Relative(
                name = name,
                phone = phone,
                relationshipDegree = relationshipDegree,
                contactIntervalDays = intervalDays,
                notes = notes,
                photoUri = photoUri
            )
            val id = repository.insertRelative(relative)
            scheduleReminderForRelative(relative.copy(id = id.toInt()))
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun deleteRelative(relative: Relative) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRelative(relative)
            cancelReminderForRelative(relative.id)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun scheduleReminderForRelative(relative: Relative) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workManager = WorkManager.getInstance(getApplication())
                val data = Data.Builder()
                    .putString("relative_name", relative.name)
                    .putString("relationship_degree", relative.relationshipDegree)
                    .putInt("relative_id", relative.id)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                    relative.contactIntervalDays.toLong(), TimeUnit.DAYS
                )
                    .setInputData(data)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    "reminder_${relative.id}",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelReminderForRelative(relativeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workManager = WorkManager.getInstance(getApplication())
                workManager.cancelUniqueWork("reminder_$relativeId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setupDueNotificationsWorker() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workManager = WorkManager.getInstance(getApplication())

                // 1. Periodic worker: Runs every 2 hours to check for due relatives
                val periodicWork = PeriodicWorkRequestBuilder<PeriodicDueWorker>(2, TimeUnit.HOURS)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    "sila_periodic_due_check",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWork
                )

                // 2. Immediate worker: Runs right now to trigger immediate notification for due relatives today
                val immediateWork = OneTimeWorkRequestBuilder<PeriodicDueWorker>().build()
                workManager.enqueueUniqueWork(
                    "sila_immediate_due_check",
                    ExistingWorkPolicy.REPLACE,
                    immediateWork
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun recordCommunication(relativeId: Int, type: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = CommunicationLog(
                relativeId = relativeId,
                type = type,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.insertLog(log)

            // Cancel any active due notification for this relative
            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(relativeId + 10000)

            // Re-trigger due check
            setupDueNotificationsWorker()

            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun insertTemplate(template: QuickTemplate) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTemplate(template)
        }
    }

    fun updateRelativeInterval(relative: Relative, newIntervalDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = relative.copy(contactIntervalDays = newIntervalDays)
            repository.updateRelative(updated)
            scheduleReminderForRelative(updated)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun editRelative(
        original: Relative,
        newName: String,
        newPhone: String,
        newDegree: String,
        newIntervalDays: Int,
        newNotes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = original.copy(
                name = newName,
                phone = newPhone,
                relationshipDegree = newDegree,
                contactIntervalDays = newIntervalDays,
                notes = newNotes
            )
            repository.updateRelative(updated)
            scheduleReminderForRelative(updated)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    // Helper functions to check status
    fun getRelativeStatus(relative: Relative): RelativeStatus {
        if (relative.lastContactDate == 0L) {
            return RelativeStatus.NEEDS_CONTACT_URGENT // "متأخر جداً - لم يتم الاتصال به مطلقاً"
        }
        val diffMs = System.currentTimeMillis() - relative.lastContactDate
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        val interval = relative.contactIntervalDays

        return when {
            diffDays >= interval + 7 -> RelativeStatus.OVERDUE_CRITICAL
            diffDays >= interval -> RelativeStatus.NEEDS_CONTACT
            diffDays >= interval / 2 -> RelativeStatus.OK_SOON
            else -> RelativeStatus.CONNECTED
        }
    }

    // --- SMART FEATURE 2: Auto-suggest Relationship Degree ---
    fun suggestRelationshipDegree(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("ماما") || lower.contains("امي") || lower.contains("أمي") || lower.contains("والدتي") ||
            lower.contains("بابا") || lower.contains("ابي") || lower.contains("أبي") || lower.contains("والدي") -> "والدان"
            
            lower.contains("اخي") || lower.contains("أخي") || lower.contains("اختي") || lower.contains("أختي") ||
            lower.contains("شقيقي") || lower.contains("شقيقتي") -> "أشقاء"
            
            lower.contains("عمي") || lower.contains("عمتي") || lower.contains("خالي") || lower.contains("خالتي") ||
            lower.contains("عم") || lower.contains("عمة") || lower.contains("خال") || lower.contains("خالة") -> "أعمام/أخوال"
            
            else -> "أقارب آخرون"
        }
    }

    // --- SMART FEATURE 3: Smart Message Generator ---
    fun generateLocalSmartMessage(relativeName: String, relationshipDegree: String, occasion: String): String {
        val greeting = when (relationshipDegree) {
            "والدان" -> "تاج رأسي وقرة عيني الغالي ${relativeName}"
            "أشقاء" -> "أخي الغالي وسندي ${relativeName}"
            "أعمام/أخوال" -> "عمي/خالي العزيز ${relativeName}"
            else -> "الغالي ${relativeName}"
        }

        return when (occasion) {
            "يوم الجمعة" -> "السلام عليكم ورحمة الله وبركاته يا $greeting. في هذا يوم الجمعة المبارك، أسأل الله أن يملأ قلبكم بالأنوار، ويحفظكم من كل مكروه، ويتقبل طاعاتكم وصالح أعمالكم. جمعة مباركة وطيبة ✨"
            "عيد الفطر/الأضحى" -> "السلام عليكم ورحمة الله وبركاته يا $greeting. أتقدم إليكم بأصدق التهاني وأطيب التبريكات بمناسبة حلول العيد المبارك، سائلاً المولى عز وجل أن يتقبل منا ومنكم صالح الأعمال، وأن يعيده علينا وعليكم بالخير واليمن والمسرات والبركات 🌸"
            "سؤال عام عن الحال" -> "السلام عليكم يا $greeting. أردت الاطمئنان على أحوالكم وصحتكم، عساكم بألف خير ونعمة دائماً. مشتاقون لسماع أخباركم الطيبة ورؤيتكم في أقرب فرصة. دمتم سالمين 🤍"
            "دعاء بالشفاء" -> "السلام عليكم ورحمة الله وبركاته يا $greeting. بلغني وعكتكم الصحية، وأسأل الله العظيم رب العرش العظيم أن يشفيك شفاءً لا يغادر سقماً، وأن يلبسك ثوب الصحة والعافية والوقار، طهور ونور إن شاء الله 🤲"
            else -> "السلام عليكم يا $greeting. أتمنى لكم يوماً جميلاً مليئاً بالخير والتوفيق والمسرات، دمتم في حفظ الله ورعايته."
        }
    }

    // --- SMART FEATURE 4: Family Time Capsule (Memories) ---
    fun addFamilyMemory(relativeId: Int, relativeName: String, title: String, description: String, imagePath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val memory = FamilyMemory(
                relativeId = relativeId,
                relativeName = relativeName,
                title = title,
                description = description,
                imagePath = imagePath,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMemory(memory)
        }
    }

    fun deleteFamilyMemory(memory: FamilyMemory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemory(memory)
        }
    }
}

enum class RelativeStatus(val label: String, val labelEn: String, val colorHex: String) {
    NEEDS_CONTACT_URGENT("تواصل الآن (لم يتصل قط)", "Contact Now (Never Called)", "E53935"),
    OVERDUE_CRITICAL("تأخرت كثيراً في الوصل!", "Very Overdue!", "D32F2F"),
    NEEDS_CONTACT("حان وقت الصلة اليوم", "Time to Connect Today", "EF6C00"),
    OK_SOON("تواصل معه قريباً", "Connect Soon", "FBC02D"),
    CONNECTED("أحسنت! متصل مؤخراً", "Great! Recently Connected", "2E7D32");

    fun getLabel(lang: String) = if (lang == "en") labelEn else label
}

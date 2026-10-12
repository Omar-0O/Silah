package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.datastore.UserPreferencesRepository
import com.example.core.util.SilaLogger
import com.example.data.AppDatabase
import com.example.data.BackupManager
import com.example.data.CallLogManager
import com.example.data.CommunicationLog
import com.example.data.FamilyMemory
import com.example.data.QuickTemplate
import com.example.data.Relative
import com.example.data.RelativeRepository
import com.example.data.model.DeviceContact
import com.example.data.model.RelativeStatus
import com.example.domain.usecase.*
import com.example.work.ReminderScheduler
import com.example.widget.SilaAppWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import javax.inject.Inject

typealias RelativeStatus = com.example.data.model.RelativeStatus
typealias DeviceContact = com.example.data.model.DeviceContact

sealed interface AppAction {
    data object ExportBackup : AppAction
    data object ImportBackup : AppAction
    data object PickContact : AppAction
}

@HiltViewModel
class RelativeViewModel @Inject constructor(
    application: Application,
    private val repository: RelativeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getFilteredRelativesUseCase: GetFilteredRelativesUseCase,
    private val calculateRelativeStatusUseCase: CalculateRelativeStatusUseCase,
    private val syncCallLogsUseCase: SyncCallLogsUseCase,
    private val backupRestoreUseCase: BackupRestoreUseCase,
    private val manageRemindersUseCase: ManageRemindersUseCase,
    private val classifyRelationshipUseCase: ClassifyRelationshipUseCase,
    private val generateMessageUseCase: GenerateMessageUseCase
) : AndroidViewModel(application) {

    // Secondary constructor for testing compatibility
    constructor(application: Application) : this(
        application = application,
        repository = RelativeRepository(
            AppDatabase.getDatabase(application).relativeDao(),
            AppDatabase.getDatabase(application).communicationLogDao(),
            AppDatabase.getDatabase(application).quickTemplateDao(),
            AppDatabase.getDatabase(application).familyMemoryDao()
        ),
        userPreferencesRepository = UserPreferencesRepository(application),
        getFilteredRelativesUseCase = GetFilteredRelativesUseCase(),
        calculateRelativeStatusUseCase = CalculateRelativeStatusUseCase(),
        syncCallLogsUseCase = SyncCallLogsUseCase(
            RelativeRepository(
                AppDatabase.getDatabase(application).relativeDao(),
                AppDatabase.getDatabase(application).communicationLogDao(),
                AppDatabase.getDatabase(application).quickTemplateDao(),
                AppDatabase.getDatabase(application).familyMemoryDao()
            )
        ),
        backupRestoreUseCase = BackupRestoreUseCase(
            RelativeRepository(
                AppDatabase.getDatabase(application).relativeDao(),
                AppDatabase.getDatabase(application).communicationLogDao(),
                AppDatabase.getDatabase(application).quickTemplateDao(),
                AppDatabase.getDatabase(application).familyMemoryDao()
            )
        ),
        manageRemindersUseCase = ManageRemindersUseCase(application),
        classifyRelationshipUseCase = ClassifyRelationshipUseCase(),
        generateMessageUseCase = GenerateMessageUseCase()
    )

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Raw database state flows
    val relatives: StateFlow<List<Relative>> = repository.allRelatives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CommunicationLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<QuickTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<FamilyMemory>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streakDays: StateFlow<Int> = logs.combine(relatives) { logsList, _ ->
        com.example.utils.DateUtils.calculateStreak(logsList.map { it.timestamp })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val last7DaysActivity: StateFlow<List<Boolean>> = logs.combine(relatives) { logsList, _ ->
        com.example.utils.DateUtils.getLast7DaysActivity(logsList.map { it.timestamp })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { false })

    // Search & Filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")

    val filteredRelatives: StateFlow<List<Relative>> = getFilteredRelativesUseCase(
        relatives = relatives,
        query = searchQuery,
        category = selectedCategory
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog & Navigation Triggers
    val showAddRelativeDialog = MutableStateFlow(false)
    val showEditRelativeDialog = MutableStateFlow<Relative?>(null)
    val showSettingsDialog = MutableStateFlow(false)
    val showImportContactsDialog = MutableStateFlow(false)
    val showRecordLogDialog = MutableStateFlow<Relative?>(null)
    val showLogsHistoryDialog = MutableStateFlow<Relative?>(null)
    val showQuickTemplatesDialog = MutableStateFlow<Relative?>(null)
    val showSetReminderDialog = MutableStateFlow<Relative?>(null)
    val showSupportSilaDialog = MutableStateFlow(false)
    val activeMilestoneDialog = MutableStateFlow<Int?>(null)
    val showWhyKinshipScreen = MutableStateFlow(false)
    val showKinshipCheckInDialog = MutableStateFlow(false)
    val pendingNotifiedRelatives = MutableStateFlow<List<Relative>>(emptyList())
    val selectedRelativeForDetail = MutableStateFlow<Relative?>(null)

    // User Preferences
    val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val selectedLanguage = MutableStateFlow(prefs.getString("selected_language", "ar") ?: "ar")
    val prefNotifyDueRelatives = MutableStateFlow(prefs.getBoolean("pref_notify_due_relatives", true))
    val prefNotifyEncouragement = MutableStateFlow(prefs.getBoolean("pref_notify_encouragement", true))
    val prefNotifyMonthly = MutableStateFlow(prefs.getBoolean("pref_notify_monthly", true))
    val reminderHour = MutableStateFlow(prefs.getInt("reminder_hour", 10))
    val reminderMinute = MutableStateFlow(prefs.getInt("reminder_minute", 0))
    val userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userGender = MutableStateFlow(prefs.getString("user_gender", "male") ?: "male")
    val userAvatarId = MutableStateFlow(prefs.getString("user_avatar_id", "avatar_01") ?: "avatar_01")
    val userPhotoPath = MutableStateFlow(prefs.getString("user_photo_path", null))
    val userPhotoTimestamp = MutableStateFlow(prefs.getLong("user_photo_ts", 0L))
    val isSyncingCallLogs = MutableStateFlow(false)
    val backupResult = MutableStateFlow<BackupManager.BackupResult?>(null)

    // Device contacts state
    val deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val isLoadingContacts = MutableStateFlow(false)
    val pickedContactName = MutableStateFlow("")
    val pickedContactPhone = MutableStateFlow("")
    val hasPendingPickedContact = MutableStateFlow(false)

    fun onContactPicked(name: String, phone: String) {
        pickedContactName.value = name
        pickedContactPhone.value = phone
        hasPendingPickedContact.value = true
    }

    fun clearPickedContact() {
        pickedContactName.value = ""
        pickedContactPhone.value = ""
        hasPendingPickedContact.value = false
    }

    // Navigation & Activity Actions
    private val actionChannel = Channel<AppAction>(Channel.BUFFERED)
    val appActions = actionChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { p ->
                isDarkMode.value = p.isDarkMode
                selectedLanguage.value = p.selectedLanguage
                prefNotifyDueRelatives.value = p.prefNotifyDueRelatives
                prefNotifyEncouragement.value = p.prefNotifyEncouragement
                prefNotifyMonthly.value = p.prefNotifyMonthly
                reminderHour.value = p.reminderHour
                reminderMinute.value = p.reminderMinute
                userName.value = p.userName
                userGender.value = p.userGender
                userAvatarId.value = p.userAvatarId
                userPhotoPath.value = p.userPhotoPath
                userPhotoTimestamp.value = p.userPhotoTimestamp
            }
        }
    }

    // CRUD for Relatives
    fun addRelative(
        name: String,
        phone: String,
        relationshipDegree: String,
        intervalDays: Int,
        notes: String = "",
        photoUri: String? = null
    ) {
        searchQuery.value = ""
        selectedCategory.value = "الكل"
        viewModelScope.launch(Dispatchers.IO) {
            val relative = Relative(
                name = name.trim(),
                phone = phone.trim(),
                relationshipDegree = relationshipDegree.trim(),
                contactIntervalDays = intervalDays,
                notes = notes.trim(),
                photoUri = photoUri
            )
            val id = repository.insertRelative(relative)
            val inserted = relative.copy(id = id.toInt())
            manageRemindersUseCase.scheduleReminderForRelative(inserted)
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
                name = newName.trim(),
                phone = newPhone.trim(),
                relationshipDegree = newDegree.trim(),
                contactIntervalDays = newIntervalDays,
                notes = newNotes.trim()
            )
            repository.updateRelative(updated)
            manageRemindersUseCase.scheduleReminderForRelative(updated)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun deleteRelative(relative: Relative) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRelative(relative)
            manageRemindersUseCase.cancelReminderForRelative(relative.id)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun updateRelativeInterval(relative: Relative, newIntervalDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = relative.copy(contactIntervalDays = newIntervalDays)
            repository.updateRelative(updated)
            manageRemindersUseCase.scheduleReminderForRelative(updated)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    // Use Case Delegations
    fun getRelativeStatus(relative: Relative): RelativeStatus = calculateRelativeStatusUseCase(relative)
    fun suggestRelationshipDegree(name: String): String = classifyRelationshipUseCase(name)
    fun generateLocalSmartMessage(name: String, degree: String, occasion: String): String =
        generateMessageUseCase(name, degree, occasion)

    fun scheduleReminderForRelative(relative: Relative) = manageRemindersUseCase.scheduleReminderForRelative(relative)
    fun cancelReminderForRelative(relativeId: Int) = manageRemindersUseCase.cancelReminderForRelative(relativeId)
    fun setupDueNotificationsWorker() = manageRemindersUseCase.setupDueNotificationsWorker()

    // Sync & Backup
    fun syncCallLogsWithRelatives(context: Context, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            isSyncingCallLogs.value = true
            try {
                val synced = syncCallLogsUseCase(context, relatives.value)
                if (synced > 0) SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
                onComplete?.invoke(synced)
            } catch (e: Exception) {
                SilaLogger.e("RelativeViewModel", "Failed to sync call logs", e)
                onComplete?.invoke(0)
            } finally {
                isSyncingCallLogs.value = false
            }
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            backupResult.value = backupRestoreUseCase.exportBackup(
                context, uri, relatives.value, logs.value, templates.value
            )
        }
    }

    fun importBackup(context: Context, uri: Uri, strategy: BackupManager.ImportConflictStrategy = BackupManager.ImportConflictStrategy.MERGE_NEW_ONLY) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = backupRestoreUseCase.importBackup(context, uri, strategy)
            backupResult.value = result
            if (result.success) SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun clearBackupResult() { backupResult.value = null }
    fun suggestedBackupName(): String = BackupManager.suggestedFileName()

    // Navigation & Dialog Helpers
    fun selectRelativeForDetail(relative: Relative) { selectedRelativeForDetail.value = relative }
    fun clearSelectedRelative() { selectedRelativeForDetail.value = null }
    fun openWhyKinshipScreen() { showWhyKinshipScreen.value = true }
    fun closeWhyKinshipScreen() { showWhyKinshipScreen.value = false }
    fun openSettingsDialog() { showSettingsDialog.value = true }
    fun closeSettingsDialog() { showSettingsDialog.value = false }
    fun openSupportSilaDialog() { showSupportSilaDialog.value = true }
    fun openAddRelativeDialog() { showAddRelativeDialog.value = true }

    fun selectRelativeById(id: Int) {
        viewModelScope.launch {
            val relative = repository.getRelativeById(id)
            if (relative != null) {
                selectedRelativeForDetail.value = relative
            }
        }
    }

    fun triggerExport() { viewModelScope.launch { actionChannel.send(AppAction.ExportBackup) } }
    fun triggerImport() { viewModelScope.launch { actionChannel.send(AppAction.ImportBackup) } }
    fun triggerContactPicker() { viewModelScope.launch { actionChannel.send(AppAction.PickContact) } }
    fun launchContactPicker() { triggerContactPicker() }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        viewModelScope.launch { userPreferencesRepository.setDarkMode(enabled) }
    }

    fun selectLanguage(langCode: String) {
        selectedLanguage.value = langCode
        viewModelScope.launch { userPreferencesRepository.setSelectedLanguage(langCode) }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        reminderHour.value = hour
        reminderMinute.value = minute
        viewModelScope.launch { userPreferencesRepository.setReminderTime(hour, minute) }
        ReminderScheduler.scheduleDailyReminder(getApplication())
    }

    fun toggleNotifyDueRelatives(enabled: Boolean) {
        prefNotifyDueRelatives.value = enabled
        viewModelScope.launch { userPreferencesRepository.setPrefNotifyDueRelatives(enabled) }
        if (enabled) {
            ReminderScheduler.scheduleDailyReminder(getApplication())
        } else {
            ReminderScheduler.cancelDailyReminder(getApplication())
        }
    }

    fun toggleNotifyEncouragement(enabled: Boolean) {
        prefNotifyEncouragement.value = enabled
        viewModelScope.launch { userPreferencesRepository.setPrefNotifyEncouragement(enabled) }
    }

    fun toggleNotifyMonthly(enabled: Boolean) {
        prefNotifyMonthly.value = enabled
        viewModelScope.launch { userPreferencesRepository.setPrefNotifyMonthly(enabled) }
    }

    fun saveUserProfile(name: String, gender: String) {
        userName.value = name
        userGender.value = gender
        viewModelScope.launch { userPreferencesRepository.setUserProfile(name, gender) }
    }

    fun saveUserAvatar(avatarId: String) {
        userAvatarId.value = avatarId
        viewModelScope.launch { userPreferencesRepository.setUserAvatarId(avatarId) }
    }

    fun setUserPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val inputStream = app.contentResolver.openInputStream(uri) ?: return@launch
                val file = File(app.filesDir, "user_photo_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val ts = System.currentTimeMillis()
                userPhotoPath.value = file.absolutePath
                userPhotoTimestamp.value = ts
                viewModelScope.launch { userPreferencesRepository.setUserPhoto(file.absolutePath, ts) }
            } catch (e: Exception) {
                SilaLogger.e("RelativeViewModel", "Failed to save user photo", e)
            }
        }
    }

    fun removeUserPhoto() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentPath = userPhotoPath.value
                if (currentPath != null) {
                    val file = File(currentPath)
                    if (file.exists()) file.delete()
                }
                val ts = System.currentTimeMillis()
                userPhotoPath.value = null
                userPhotoTimestamp.value = ts
                userAvatarId.value = "avatar_01"
                viewModelScope.launch {
                    userPreferencesRepository.setUserPhoto(null, ts)
                    userPreferencesRepository.setUserAvatarId("avatar_01")
                }
            } catch (e: Exception) {
                SilaLogger.e("RelativeViewModel", "Failed to remove user photo", e)
            }
        }
    }

    fun recordCommunication(relativeId: Int, type: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = CommunicationLog(relativeId = relativeId, type = type, notes = notes, timestamp = System.currentTimeMillis())
            repository.insertLog(log)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun getLogsForRelative(relativeId: Int) = repository.getLogsForRelative(relativeId)

    fun confirmContactedFromDialog(relativeId: Int, notes: String = "تم تأكيد التواصل عند فتح التطبيق") {
        viewModelScope.launch(Dispatchers.IO) {
            val relative = repository.getRelativeById(relativeId) ?: return@launch
            repository.insertLog(CommunicationLog(relativeId = relativeId, type = "تواصل مؤكد", notes = notes, timestamp = System.currentTimeMillis()))
            ReminderScheduler.removePendingNotifiedRelative(getApplication(), relativeId)
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun dismissKinshipCheckInDialog() {
        showKinshipCheckInDialog.value = false
    }

    fun confirmAllContactedFromDialog() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val currentPending = pendingNotifiedRelatives.value
            val context = getApplication<Application>()

            for (relative in currentPending) {
                val log = CommunicationLog(
                    relativeId = relative.id,
                    type = "مكالمة هاتفية",
                    notes = "تم تأكيد التواصل عند فتح التطبيق",
                    timestamp = now
                )
                repository.insertLog(log)
                repository.updateRelative(relative.copy(lastContactDate = now))
                ReminderScheduler.removePendingNotifiedRelative(context, relative.id)
            }

            SilaAppWidgetProvider.triggerWidgetUpdate(context)
            pendingNotifiedRelatives.value = emptyList()
            showKinshipCheckInDialog.value = false
        }
    }

    fun checkPendingNotifiedRelatives() {
        checkPendingNotifiedRelatives(getApplication())
    }

    fun checkPendingNotifiedRelatives(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pendingIds = ReminderScheduler.getPendingNotifiedRelativeIds(context)
                if (pendingIds.isEmpty()) {
                    pendingNotifiedRelatives.value = emptyList()
                    showKinshipCheckInDialog.value = false
                    return@launch
                }

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfToday = cal.timeInMillis

                val allRelatives = repository.getAllRelativesOnce()
                val uncontactedPending = allRelatives.filter { relative ->
                    pendingIds.contains(relative.id) && relative.lastContactDate < startOfToday
                }

                pendingNotifiedRelatives.value = uncontactedPending
                showKinshipCheckInDialog.value = uncontactedPending.isNotEmpty()
            } catch (e: Exception) {
                SilaLogger.e("RelativeViewModel", "Failed to check pending relatives", e)
            }
        }
    }

    fun fetchDeviceContacts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingContacts.value = true
            val contactsList = mutableListOf<DeviceContact>()
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )

            try {
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoUriIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                    while (cursor.moveToNext()) {
                        if (nameIndex != -1 && numberIndex != -1) {
                            val name = cursor.getString(nameIndex)?.trim() ?: ""
                            val number = cursor.getString(numberIndex)?.trim() ?: ""
                            val photoUri = if (photoUriIndex != -1) cursor.getString(photoUriIndex) else null
                            if (name.isNotEmpty() && number.isNotEmpty()) {
                                contactsList.add(DeviceContact(name = name, phone = number, isGoogle = false, photoUri = photoUri))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                SilaLogger.e("RelativeViewModel", "Failed to query contacts", e)
            }

            deviceContacts.value = contactsList
                .distinctBy { CallLogManager.normalizePhoneNumber(it.phone).ifEmpty { it.phone } }
                .sortedBy { it.name }
            isLoadingContacts.value = false
        }
    }
}

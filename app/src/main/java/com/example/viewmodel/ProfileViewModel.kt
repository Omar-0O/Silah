package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.datastore.UserPreferencesRepository
import com.example.core.util.SilaLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private fun safeGetString(key: String, default: String): String {
        return try { prefs.getString(key, default) ?: default } catch (e: Exception) { default }
    }
    private fun safeGetNullableString(key: String): String? {
        return try { prefs.getString(key, null) } catch (e: Exception) { null }
    }
    private fun safeGetLong(key: String, default: Long): Long {
        return try { prefs.getLong(key, default) } catch (e: Exception) { default }
    }

    val userName = MutableStateFlow(safeGetString("user_name", ""))
    val userGender = MutableStateFlow(safeGetString("user_gender", "male"))
    val userAvatarId = MutableStateFlow(safeGetString("user_avatar_id", "avatar_01"))

    private fun initUserPhotoPath(): String? {
        val savedPath = safeGetNullableString("user_photo_path")
        if (!savedPath.isNullOrBlank() && File(savedPath).exists()) {
            return savedPath
        }
        val legacy = File(getApplication<Application>().filesDir, "custom_avatar.jpg")
        if (legacy.exists() && legacy.length() > 0) {
            return legacy.absolutePath
        }
        return null
    }

    val userPhotoPath = MutableStateFlow<String?>(initUserPhotoPath())
    val userPhotoTimestamp = MutableStateFlow<Long>(safeGetLong("user_photo_ts", System.currentTimeMillis()))

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { p ->
                userName.value = p.userName
                userGender.value = p.userGender
                userAvatarId.value = p.userAvatarId
                userPhotoPath.value = p.userPhotoPath
                userPhotoTimestamp.value = p.userPhotoTimestamp
            }
        }
    }

    fun saveUserProfile(name: String, gender: String) {
        userName.value = name
        userGender.value = gender
        prefs.edit()
            .putString("user_name", name)
            .putString("user_gender", gender)
            .apply()
        viewModelScope.launch { userPreferencesRepository.setUserProfile(name, gender) }
    }

    fun saveUserAvatar(avatarId: String) {
        userAvatarId.value = avatarId
        prefs.edit().putString("user_avatar_id", avatarId).apply()
        viewModelScope.launch { userPreferencesRepository.setUserAvatarId(avatarId) }
    }

    fun setUserPhoto(sourceUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val inputStream = openStreamSafely(app, sourceUri) ?: return@launch
                val file = File(app.filesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val ts = System.currentTimeMillis()
                prefs.edit()
                    .putString("user_photo_path", file.absolutePath)
                    .putLong("user_photo_ts", ts)
                    .apply()
                userPhotoPath.value = file.absolutePath
                userPhotoTimestamp.value = ts
                viewModelScope.launch { userPreferencesRepository.setUserPhoto(file.absolutePath, ts) }
            } catch (e: Exception) {
                SilaLogger.e("ProfileViewModel", "Failed to save user photo from Uri", e)
            }
        }
    }

    fun setUserPhotoFromFile(file: File) {
        val ts = System.currentTimeMillis()
        prefs.edit()
            .putString("user_photo_path", file.absolutePath)
            .putLong("user_photo_ts", ts)
            .apply()
        userPhotoPath.value = file.absolutePath
        userPhotoTimestamp.value = ts
        viewModelScope.launch { userPreferencesRepository.setUserPhoto(file.absolutePath, ts) }
    }

    fun removeUserPhoto() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentPath = userPhotoPath.value
                if (currentPath != null) {
                    val file = File(currentPath)
                    if (file.exists()) file.delete()
                }
                val legacy = File(getApplication<Application>().filesDir, "custom_avatar.jpg")
                if (legacy.exists()) legacy.delete()

                val ts = System.currentTimeMillis()
                prefs.edit()
                    .remove("user_photo_path")
                    .putLong("user_photo_ts", ts)
                    .putString("user_avatar_id", "avatar_01")
                    .apply()
                userPhotoPath.value = null
                userPhotoTimestamp.value = ts
                userAvatarId.value = "avatar_01"
                viewModelScope.launch {
                    userPreferencesRepository.setUserPhoto(null, ts)
                    userPreferencesRepository.setUserAvatarId("avatar_01")
                }
            } catch (e: Exception) {
                SilaLogger.e("ProfileViewModel", "Failed to remove user photo", e)
            }
        }
    }

    fun openStreamSafely(app: Application, uri: Uri): java.io.InputStream? {
        try {
            val s = app.contentResolver.openInputStream(uri)
            if (s != null) return s
        } catch (_: Exception) {}

        try {
            if (uri.scheme == "content") {
                val fd = app.contentResolver.openFileDescriptor(uri, "r")
                if (fd != null) return java.io.FileInputStream(fd.fileDescriptor)
            }
        } catch (_: Exception) {}

        try {
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null) {
                    val f = File(path)
                    if (f.exists()) return java.io.FileInputStream(f)
                }
            }
        } catch (_: Exception) {}

        return null
    }
}

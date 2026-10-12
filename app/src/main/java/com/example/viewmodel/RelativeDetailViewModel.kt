package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CommunicationLog
import com.example.data.Relative
import com.example.data.RelativeRepository
import com.example.widget.SilaAppWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RelativeDetailViewModel @Inject constructor(
    application: Application,
    private val repository: RelativeRepository
) : AndroidViewModel(application) {

    val selectedRelativeForDetail = MutableStateFlow<Relative?>(null)

    fun selectRelative(relative: Relative) {
        selectedRelativeForDetail.value = relative
    }

    fun clearSelectedRelative() {
        selectedRelativeForDetail.value = null
    }

    fun getLogsForRelative(relativeId: Int): Flow<List<CommunicationLog>> {
        return repository.getLogsForRelative(relativeId)
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
            SilaAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }
}

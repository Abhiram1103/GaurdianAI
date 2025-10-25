package com.guardianai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guardianai.data.EmergencyContact
import com.guardianai.data.FallEventEntity
import com.guardianai.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository(application.applicationContext)

    val contacts: StateFlow<List<EmergencyContact>> = repo.getAllContacts()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<FallEventEntity>> = repo.getAllEvents()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContact(name: String, phone: String) {
        viewModelScope.launch {
            repo.insertContact(EmergencyContact(name = name, phone = phone))
        }
    }

    fun removeContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repo.deleteContact(contact)
        }
    }

    fun addEvent(timestamp: Long, probability: Float) {
        viewModelScope.launch {
            repo.insertEvent(FallEventEntity(timestamp = timestamp, probability = probability))
        }
    }

    fun clearEvents() {
        viewModelScope.launch { repo.clearEvents() }
    }
}

package com.guardianai.repository

import android.content.Context
import com.guardianai.data.AppDatabase
import com.guardianai.data.EmergencyContact
import com.guardianai.data.FallEventEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.appDao()

    // Contacts
    fun getAllContacts(): Flow<List<EmergencyContact>> = dao.getAllContacts()
    suspend fun insertContact(contact: EmergencyContact) = dao.insertContact(contact)
    suspend fun deleteContact(contact: EmergencyContact) = dao.deleteContact(contact)

    // Fall events
    fun getAllEvents(): Flow<List<FallEventEntity>> = dao.getAllEvents()
    suspend fun insertEvent(event: FallEventEntity) = dao.insertEvent(event)
    suspend fun clearEvents() = dao.clearEvents()
}

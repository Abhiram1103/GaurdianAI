package com.guardianai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Contacts
    @Query("SELECT * FROM emergency_contacts ORDER BY id DESC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert
    suspend fun insertContact(contact: EmergencyContact): Long

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    // Fall events
    @Query("SELECT * FROM fall_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<FallEventEntity>>

    @Insert
    suspend fun insertEvent(event: FallEventEntity): Long

    @Query("DELETE FROM fall_events")
    suspend fun clearEvents()
}

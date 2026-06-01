package com.example.timereminder.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.timereminder.data.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY hour * 60 + minute ASC, created_at DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 ORDER BY hour * 60 + minute ASC")
    fun getEnabledReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderByIdFlow(id: Long): Flow<ReminderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    suspend fun getAllEnabledReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders ORDER BY hour * 60 + minute ASC LIMIT :limit")
    suspend fun getRecentReminders(limit: Int = 5): List<ReminderEntity>
}

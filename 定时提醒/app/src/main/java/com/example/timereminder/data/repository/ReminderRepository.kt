package com.example.timereminder.data.repository

import com.example.timereminder.data.db.dao.ReminderDao
import com.example.timereminder.data.db.entity.ReminderEntity
import com.example.timereminder.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 提醒数据仓库
 */
class ReminderRepository(private val reminderDao: ReminderDao) {

    /** 获取所有提醒（按时间排序） */
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders().map { entities ->
        entities.map { it.toDomainModel() }
    }

    /** 获取已启用的提醒 */
    val enabledReminders: Flow<List<Reminder>> = reminderDao.getEnabledReminders().map { entities ->
        entities.map { it.toDomainModel() }
    }

    /** 按ID获取提醒 */
    suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)?.toDomainModel()
    }

    /** 按ID获取提醒（Flow） */
    fun getReminderByIdFlow(id: Long): Flow<Reminder?> {
        return reminderDao.getReminderByIdFlow(id).map { it?.toDomainModel() }
    }

    /** 保存提醒（插入或更新） */
    suspend fun saveReminder(reminder: Reminder): Long {
        val entity = ReminderEntity.fromDomainModel(reminder)
        return if (reminder.id == 0L) {
            reminderDao.insert(entity)
        } else {
            reminderDao.update(entity)
            reminder.id
        }
    }

    /** 删除提醒 */
    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.delete(ReminderEntity.fromDomainModel(reminder))
    }

    /** 按ID删除 */
    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteById(id)
    }

    /** 获取所有已启用的提醒（挂起，非Flow） */
    suspend fun getAllEnabledReminders(): List<Reminder> {
        return reminderDao.getAllEnabledReminders().map { it.toDomainModel() }
    }

    /** 获取最近的提醒（用于小部件） */
    suspend fun getRecentReminders(limit: Int = 5): List<Reminder> {
        return reminderDao.getRecentReminders(limit).map { it.toDomainModel() }
    }
}

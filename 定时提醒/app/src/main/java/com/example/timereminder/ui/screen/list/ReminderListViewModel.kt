package com.example.timereminder.ui.screen.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timereminder.alarm.AlarmScheduler
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.data.repository.TagRepository
import com.example.timereminder.domain.model.Reminder
import com.example.timereminder.domain.model.Tag
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderListViewModel(
    private val reminderRepository: ReminderRepository,
    private val tagRepository: TagRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = reminderRepository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<Tag>> = tagRepository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 切换提醒启用/禁用
     */
    fun toggleEnabled(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            reminderRepository.saveReminder(updated)

            if (updated.isEnabled) {
                alarmScheduler.scheduleReminder(updated)
            } else {
                alarmScheduler.cancelReminder(reminder.id)
            }
        }
    }

    /**
     * 删除提醒
     */
    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            alarmScheduler.cancelReminder(reminderId)
            reminderRepository.deleteReminderById(reminderId)
        }
    }

    class Factory(
        private val reminderRepository: ReminderRepository,
        private val tagRepository: TagRepository,
        private val alarmScheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderListViewModel(reminderRepository, tagRepository, alarmScheduler) as T
        }
    }
}

package com.example.timereminder.ui.screen.edit

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timereminder.alarm.AlarmScheduler
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.data.repository.TagRepository
import com.example.timereminder.domain.model.Reminder
import com.example.timereminder.domain.model.ReminderType
import com.example.timereminder.domain.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 提醒编辑页面状态
 */
data class ReminderEditState(
    val title: String = "",
    val description: String = "",
    val hour: Int = 8,
    val minute: Int = 0,
    val type: ReminderType = ReminderType.ONCE,
    val intervalMinutes: String = "",
    val selectedDays: Set<Int> = emptySet(),
    val dayOfMonth: String = "",
    val isNotificationEnabled: Boolean = true,
    val isRingEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    /** 铃声显示名称 */
    val ringtoneDisplayName: String = "默认系统闹钟铃声",
    val selectedTagId: Long? = null,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false
)

class ReminderEditViewModel(
    private val reminderRepository: ReminderRepository,
    private val tagRepository: TagRepository,
    private val alarmScheduler: AlarmScheduler,
    private val reminderId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderEditState())
    val state: StateFlow<ReminderEditState> = _state.asStateFlow()

    val tags: StateFlow<List<Tag>> = tagRepository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 保存完成回调 */
    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted: StateFlow<Boolean> = _saveCompleted.asStateFlow()

    init {
        if (reminderId != null && reminderId > 0) {
            loadReminder(reminderId)
        }
    }

    private fun loadReminder(id: Long) {
        viewModelScope.launch {
            val reminder = reminderRepository.getReminderById(id)
            if (reminder != null) {
                _state.value = ReminderEditState(
                    title = reminder.title,
                    description = reminder.description ?: "",
                    hour = reminder.hour,
                    minute = reminder.minute,
                    type = reminder.type,
                    intervalMinutes = reminder.intervalMinutes?.toString() ?: "",
                    selectedDays = if (reminder.dayOfWeek != null) {
                        (0..6).filter { reminder.dayOfWeek and (1 shl it) != 0 }.toSet()
                    } else emptySet(),
                    dayOfMonth = reminder.dayOfMonth?.toString() ?: "",
                    isNotificationEnabled = reminder.isNotificationEnabled,
                    isRingEnabled = reminder.isRingEnabled,
                    isVibrationEnabled = reminder.isVibrationEnabled,
                    ringtoneUri = reminder.ringtoneUri,
                    ringtoneDisplayName = if (reminder.ringtoneUri != null) "自定义铃声" else "默认系统闹钟铃声",
                    selectedTagId = reminder.tagId,
                    isLoaded = true
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun updateTime(hour: Int, minute: Int) {
        _state.value = _state.value.copy(hour = hour, minute = minute)
    }

    fun updateType(type: ReminderType) {
        _state.value = _state.value.copy(type = type)
    }

    fun updateIntervalMinutes(minutes: String) {
        _state.value = _state.value.copy(intervalMinutes = minutes)
    }

    fun toggleDay(dayIndex: Int) {
        val current = _state.value.selectedDays
        _state.value = _state.value.copy(
            selectedDays = if (dayIndex in current) current - dayIndex else current + dayIndex
        )
    }

    fun updateDayOfMonth(day: String) {
        _state.value = _state.value.copy(dayOfMonth = day)
    }

    fun toggleNotification() {
        _state.value = _state.value.copy(isNotificationEnabled = !_state.value.isNotificationEnabled)
    }

    fun toggleRing() {
        _state.value = _state.value.copy(isRingEnabled = !_state.value.isRingEnabled)
    }

    fun toggleVibration() {
        _state.value = _state.value.copy(isVibrationEnabled = !_state.value.isVibrationEnabled)
    }

    fun updateRingtone(uri: String?, displayName: String) {
        _state.value = _state.value.copy(
            ringtoneUri = uri,
            ringtoneDisplayName = displayName
        )
    }

    fun resetRingtoneToDefault() {
        _state.value = _state.value.copy(
            ringtoneUri = null,
            ringtoneDisplayName = "默认系统闹钟铃声"
        )
    }

    fun resolveRingtoneName(contentResolver: ContentResolver, uriString: String?): String {
        if (uriString == null) return "默认系统闹钟铃声"
        return try {
            val ringtoneUri = Uri.parse(uriString)
            // 尝试从 ContentResolver 获取文件名（适用于文件选择器选中的本地文件）
            var name = "自定义铃声"
            contentResolver.query(ringtoneUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex) ?: "自定义铃声"
                    }
                }
            }
            name
        } catch (e: Exception) {
            "自定义铃声"
        }
    }

    fun updateTagId(tagId: Long?) {
        _state.value = _state.value.copy(selectedTagId = tagId)
    }

    /**
     * 保存提醒
     */
    fun saveReminder() {
        val s = _state.value
        if (s.title.isBlank()) return

        viewModelScope.launch {
            _state.value = s.copy(isSaving = true)

            val dayOfWeek = if (s.type == ReminderType.WEEKLY || s.type == ReminderType.WEEKDAY) {
                if (s.type == ReminderType.WEEKDAY) {
                    0b00011111 // 周一~周五
                } else {
                    var bitmask = 0
                    for (day in s.selectedDays) {
                        bitmask = bitmask or (1 shl day)
                    }
                    bitmask
                }
            } else null

            val reminder = Reminder(
                id = reminderId ?: 0L,
                title = s.title,
                description = s.description.ifBlank { null },
                type = s.type,
                triggerTime = if (s.type == ReminderType.ONCE) {
                    calculateTriggerTime(s.hour, s.minute)
                } else null,
                intervalMinutes = s.intervalMinutes.toLongOrNull(),
                dayOfWeek = dayOfWeek,
                dayOfMonth = s.dayOfMonth.toIntOrNull(),
                hour = s.hour,
                minute = s.minute,
                ringtoneUri = s.ringtoneUri,
                isNotificationEnabled = s.isNotificationEnabled,
                isRingEnabled = s.isRingEnabled,
                isVibrationEnabled = s.isVibrationEnabled,
                tagId = s.selectedTagId
            )

            // 取消旧闹钟
            if (reminderId != null && reminderId > 0) {
                alarmScheduler.cancelReminder(reminderId)
            }

            // 保存
            val savedId = reminderRepository.saveReminder(reminder)

            // 调度新闹钟
            val saved = reminder.copy(id = savedId)
            alarmScheduler.scheduleReminder(saved)

            _state.value = _state.value.copy(isSaving = false)
            _saveCompleted.value = true
        }
    }

    private fun calculateTriggerTime(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        // 如果时间已过，设为明天
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    class Factory(
        private val reminderRepository: ReminderRepository,
        private val tagRepository: TagRepository,
        private val alarmScheduler: AlarmScheduler,
        private val reminderId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderEditViewModel(reminderRepository, tagRepository, alarmScheduler, reminderId) as T
        }
    }
}

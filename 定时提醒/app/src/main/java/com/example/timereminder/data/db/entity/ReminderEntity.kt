package com.example.timereminder.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.timereminder.domain.model.Reminder
import com.example.timereminder.domain.model.ReminderType

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["tag_id"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val description: String? = null,

    /** 提醒类型（0=一次性, 1=每天, 2=每周, 3=每月, 4=间隔, 5=工作日） */
    @ColumnInfo(name = "type")
    val typeValue: Int = ReminderType.ONCE.value,

    /** 一次性提醒的具体触发时间戳（毫秒） */
    @ColumnInfo(name = "trigger_time")
    val triggerTime: Long? = null,

    /** 自定义间隔分钟数 */
    @ColumnInfo(name = "interval_minutes")
    val intervalMinutes: Long? = null,

    /** 每周位标记：bit0=周一 ~ bit6=周日 */
    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int? = null,

    /** 每月第几天 */
    @ColumnInfo(name = "day_of_month")
    val dayOfMonth: Int? = null,

    val hour: Int = 0,

    val minute: Int = 0,

    /** 铃声URI */
    @ColumnInfo(name = "ringtone_uri")
    val ringtoneUri: String? = null,

    /** 是否启用通知 */
    @ColumnInfo(name = "notification_enabled")
    val isNotificationEnabled: Boolean = true,

    /** 是否启用响铃 */
    @ColumnInfo(name = "ring_enabled")
    val isRingEnabled: Boolean = true,

    /** 是否启用震动 */
    @ColumnInfo(name = "vibration_enabled")
    val isVibrationEnabled: Boolean = true,

    /** 标签ID */
    @ColumnInfo(name = "tag_id")
    val tagId: Long? = null,

    /** 是否启用 */
    @ColumnInfo(name = "enabled")
    val isEnabled: Boolean = true,

    /** 是否在贪睡中 */
    @ColumnInfo(name = "snoozed")
    val isSnoozed: Boolean = false,

    /** 贪睡结束时间戳 */
    @ColumnInfo(name = "snooze_end_time")
    val snoozeEndTime: Long? = null,

    /** 创建时间 */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** 更新时间 */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** 转换为领域模型 */
    fun toDomainModel(): Reminder {
        return Reminder(
            id = id,
            title = title,
            description = description,
            type = ReminderType.fromValue(typeValue),
            triggerTime = triggerTime,
            intervalMinutes = intervalMinutes,
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            hour = hour,
            minute = minute,
            ringtoneUri = ringtoneUri,
            isNotificationEnabled = isNotificationEnabled,
            isRingEnabled = isRingEnabled,
            isVibrationEnabled = isVibrationEnabled,
            tagId = tagId,
            isEnabled = isEnabled,
            isSnoozed = isSnoozed,
            snoozeEndTime = snoozeEndTime,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /** 从领域模型创建Entity */
        fun fromDomainModel(reminder: Reminder): ReminderEntity {
            return ReminderEntity(
                id = reminder.id,
                title = reminder.title,
                description = reminder.description,
                typeValue = reminder.type.value,
                triggerTime = reminder.triggerTime,
                intervalMinutes = reminder.intervalMinutes,
                dayOfWeek = reminder.dayOfWeek,
                dayOfMonth = reminder.dayOfMonth,
                hour = reminder.hour,
                minute = reminder.minute,
                ringtoneUri = reminder.ringtoneUri,
                isNotificationEnabled = reminder.isNotificationEnabled,
                isRingEnabled = reminder.isRingEnabled,
                isVibrationEnabled = reminder.isVibrationEnabled,
                tagId = reminder.tagId,
                isEnabled = reminder.isEnabled,
                isSnoozed = reminder.isSnoozed,
                snoozeEndTime = reminder.snoozeEndTime,
                createdAt = reminder.createdAt,
                updatedAt = reminder.updatedAt
            )
        }
    }
}

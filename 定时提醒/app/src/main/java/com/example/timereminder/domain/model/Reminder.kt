package com.example.timereminder.domain.model

/**
 * 提醒领域模型
 */
data class Reminder(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val type: ReminderType = ReminderType.ONCE,
    /** 一次性提醒的具体触发时间戳（毫秒） */
    val triggerTime: Long? = null,
    /** 自定义间隔分钟数（INTERVAL类型使用） */
    val intervalMinutes: Long? = null,
    /** 每周：位标记，bit0=周一 ~ bit6=周日 */
    val dayOfWeek: Int? = null,
    /** 每月：1-31 */
    val dayOfMonth: Int? = null,
    val hour: Int = 0,
    val minute: Int = 0,
    /** 铃声URI */
    val ringtoneUri: String? = null,
    val isNotificationEnabled: Boolean = true,
    val isRingEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val tagId: Long? = null,
    val isEnabled: Boolean = true,
    val isSnoozed: Boolean = false,
    val snoozeEndTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 判断是否为周期性提醒
     */
    fun isRecurring(): Boolean = type != ReminderType.ONCE

    /**
     * 获取重复规则描述文本
     */
    fun getRepeatDescription(): String {
        return when (type) {
            ReminderType.ONCE -> "一次性"
            ReminderType.DAILY -> "每天"
            ReminderType.WEEKLY -> {
                val days = dayOfWeek?.let { decodeWeekDays(it) } ?: emptyList()
                if (days.isEmpty()) "每周" else "每周${days.joinToString("/")}"
            }
            ReminderType.MONTHLY -> "每月${dayOfMonth}号"
            ReminderType.INTERVAL -> "每隔${intervalMinutes ?: 0}分钟"
            ReminderType.WEEKDAY -> "工作日"
        }
    }

    companion object {
        private val WEEK_DAY_NAMES = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

        /**
         * 解码位标记 → 星期名称列表
         */
        fun decodeWeekDays(bitmask: Int): List<String> {
            val result = mutableListOf<String>()
            for (i in 0..6) {
                if (bitmask and (1 shl i) != 0) {
                    result.add(WEEK_DAY_NAMES[i])
                }
            }
            return result
        }

        /**
         * 编码星期名称列表 → 位标记
         */
        fun encodeWeekDays(days: List<Int>): Int {
            var bitmask = 0
            for (dayIndex in days) {
                bitmask = bitmask or (1 shl dayIndex)
            }
            return bitmask
        }
    }
}

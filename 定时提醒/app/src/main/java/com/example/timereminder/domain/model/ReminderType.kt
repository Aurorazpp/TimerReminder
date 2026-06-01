package com.example.timereminder.domain.model

/**
 * 提醒重复类型
 */
enum class ReminderType(val value: Int) {
    /** 一次性提醒 */
    ONCE(0),

    /** 每天固定时间 */
    DAILY(1),

    /** 每周固定星期几 */
    WEEKLY(2),

    /** 每月固定日期 */
    MONTHLY(3),

    /** 自定义间隔（每隔N分钟） */
    INTERVAL(4),

    /** 工作日（周一至周五） */
    WEEKDAY(5);

    companion object {
        fun fromValue(value: Int): ReminderType {
            return entries.find { it.value == value } ?: ONCE
        }
    }
}

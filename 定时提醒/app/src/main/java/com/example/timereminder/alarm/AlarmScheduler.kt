package com.example.timereminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.timereminder.domain.model.Reminder
import com.example.timereminder.domain.model.ReminderType
import java.util.Calendar

/**
 * 闹钟调度器
 * 负责设置/取消系统 AlarmManager 闹钟
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * 调度一个提醒
     */
    fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled) return
        if (!canScheduleExactAlarms()) return

        when (reminder.type) {
            ReminderType.ONCE -> scheduleOnce(reminder)
            ReminderType.DAILY -> scheduleDaily(reminder)
            ReminderType.WEEKLY -> scheduleWeekly(reminder)
            ReminderType.MONTHLY -> scheduleMonthly(reminder)
            ReminderType.INTERVAL -> scheduleInterval(reminder)
            ReminderType.WEEKDAY -> scheduleWeekday(reminder)
        }
    }

    /**
     * 取消一个提醒的闹钟
     */
    fun cancelReminder(reminderId: Long) {
        val intent = createIntent(reminderId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager?.cancel(it)
            it.cancel()
        }
    }

    /**
     * 重新调度所有已启用的提醒（设备重启后调用）
     */
    fun rescheduleAll(reminders: List<Reminder>) {
        reminders.forEach { reminder ->
            if (reminder.isEnabled && !reminder.isSnoozed) {
                if (reminder.type == ReminderType.ONCE &&
                    reminder.triggerTime != null &&
                    reminder.triggerTime <= System.currentTimeMillis()
                ) {
                    // 一次性提醒已过期，跳过
                    return@forEach
                }
                scheduleReminder(reminder)
            }
        }
    }

    /**
     * 检查是否有精确闹钟权限
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }

    // ──── 一次性提醒 ────

    private fun scheduleOnce(reminder: Reminder) {
        val triggerTime = reminder.triggerTime
        if (triggerTime == null || triggerTime <= System.currentTimeMillis()) return

        setExactAlarm(reminder.id, triggerTime)
    }

    // ──── 每日提醒 ────

    private fun scheduleDaily(reminder: Reminder) {
        val calendar = getNextDailyTrigger(reminder.hour, reminder.minute)
        setExactAlarm(reminder.id, calendar.timeInMillis)
    }

    // ──── 每周提醒 ────

    private fun scheduleWeekly(reminder: Reminder) {
        val dayOfWeek = reminder.dayOfWeek ?: return
        val calendar = getNextWeeklyTrigger(dayOfWeek, reminder.hour, reminder.minute)
        setExactAlarm(reminder.id, calendar.timeInMillis)
    }

    // ──── 每月提醒 ────

    private fun scheduleMonthly(reminder: Reminder) {
        val dayOfMonth = reminder.dayOfMonth ?: return
        val calendar = getNextMonthlyTrigger(dayOfMonth, reminder.hour, reminder.minute)
        setExactAlarm(reminder.id, calendar.timeInMillis)
    }

    // ──── 自定义间隔 ────

    private fun scheduleInterval(reminder: Reminder) {
        val intervalMinutes = reminder.intervalMinutes ?: return
        if (intervalMinutes <= 0) return

        // 首次触发时间：当前时间 + 间隔
        val triggerTime = System.currentTimeMillis() + intervalMinutes * 60 * 1000
        setExactAlarm(reminder.id, triggerTime)
    }

    // ──── 工作日提醒 ────

    private fun scheduleWeekday(reminder: Reminder) {
        // 工作日 = 周一(1) ~ 周五(5)
        val weekdayBitmask = (0b00011111) // bit0=周一 ... bit4=周五
        val calendar = getNextWeeklyTrigger(weekdayBitmask, reminder.hour, reminder.minute)
        setExactAlarm(reminder.id, calendar.timeInMillis)
    }

    // ──── 设置精确闹钟 ────

    private fun setExactAlarm(reminderId: Long, triggerAtMillis: Long) {
        val intent = createIntent(reminderId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                it.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                it.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    private fun createIntent(reminderId: Long): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminderId)
            action = "com.example.timereminder.ACTION_ALARM"
        }
    }

    // ──── 时间计算工具 ────

    /**
     * 获取下一次每日触发时间
     */
    private fun getNextDailyTrigger(hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果时间已过，设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    /**
     * 获取下一次每周触发时间
     * @param dayBitmask 位标记，bit0=周一 ~ bit6=周日
     */
    private fun getNextWeeklyTrigger(
        dayBitmask: Int,
        hour: Int,
        minute: Int
    ): Calendar {
        val now = Calendar.getInstance()
        val currentDayOfWeek = getCalendarDayOfWeek(now.get(Calendar.DAY_OF_WEEK))

        // 从今天开始检查未来7天
        for (offset in 0..6) {
            val checkDay = (currentDayOfWeek + offset) % 7
            if (dayBitmask and (1 shl checkDay) != 0) {
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 如果选中今天但时间已过，继续找下一天
                if (offset == 0 && calendar.timeInMillis <= System.currentTimeMillis()) {
                    continue
                }

                return calendar
            }
        }

        // 后备：下周第一天
        return getNextDailyTrigger(hour, minute).apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }
    }

    /**
     * 获取下一次每月触发时间
     */
    private fun getNextMonthlyTrigger(
        dayOfMonth: Int,
        hour: Int,
        minute: Int
    ): Calendar {
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_MONTH)

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (currentDay < dayOfMonth) {
                // 本月还未到该日期
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            } else {
                // 下个月
                add(Calendar.MONTH, 1)
                val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, maxDay))
            }

            // 如果计算出的时间已过（极端情况），再加一个月
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.MONTH, 1)
                val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, minOf(dayOfMonth, maxDay))
            }
        }
    }

    /**
     * 将 Calendar.DAY_OF_WEEK（1=周日...7=周六）转换为我们的格式（0=周一...6=周日）
     */
    private fun getCalendarDayOfWeek(calendarDay: Int): Int {
        // Calendar: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
        // Ours: 0=Monday, 1=Tuesday, ..., 6=Sunday
        return when (calendarDay) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }
}

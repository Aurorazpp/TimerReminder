package com.example.timereminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.domain.model.Reminder
import com.example.timereminder.domain.model.ReminderType
import com.example.timereminder.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 闹钟广播接收器
 * 接收 AlarmManager 触发的广播，执行提醒逻辑
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        // 获取 WakeLock 确保处理完成
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TimerReminder:AlarmWakeLock"
        )
        wakeLock.acquire(10_000L) // 最多持有 10 秒

        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repository = ReminderRepository(db.reminderDao())
                val reminder = repository.getReminderById(reminderId)

                if (reminder != null && reminder.isEnabled) {
                    handleAlarm(context, repository, reminder)
                }

                // 周期性提醒：重新调度下一次
                if (reminder != null && reminder.isRecurring()) {
                    val scheduler = AlarmScheduler(context)
                    scheduler.scheduleReminder(reminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }

    private suspend fun handleAlarm(
        context: Context,
        repository: ReminderRepository,
        reminder: Reminder
    ) {
        // 发送通知
        if (reminder.isNotificationEnabled) {
            NotificationHelper.sendReminderNotification(
                context,
                reminder.id,
                reminder.title,
                reminder.description
            )
        }

        // 启动响铃服务（响铃 + 震动）
        if (reminder.isRingEnabled || reminder.isVibrationEnabled) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(AlarmService.EXTRA_REMINDER_ID, reminder.id)
                putExtra(AlarmService.EXTRA_TITLE, reminder.title)
                putExtra(AlarmService.EXTRA_DESCRIPTION, reminder.description ?: "")
                putExtra(AlarmService.EXTRA_RING_ENABLED, reminder.isRingEnabled)
                putExtra(AlarmService.EXTRA_VIBRATION_ENABLED, reminder.isVibrationEnabled)
                putExtra(AlarmService.EXTRA_RINGTONE_URI, reminder.ringtoneUri)
            }
            context.startForegroundService(serviceIntent)
        }

        // 一次性提醒：触发后禁用
        if (reminder.type == ReminderType.ONCE) {
            val updated = reminder.copy(isEnabled = false)
            repository.saveReminder(updated)
        }
    }
}

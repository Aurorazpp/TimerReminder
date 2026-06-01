package com.example.timereminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 设备启动广播接收器
 * 系统重启后重新调度所有提醒的闹钟
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repository = ReminderRepository(db.reminderDao())
                val enabledReminders = repository.getAllEnabledReminders()

                val scheduler = AlarmScheduler(context)
                scheduler.rescheduleAll(enabledReminders)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

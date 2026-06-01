package com.example.timereminder

import android.app.Application
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerReminderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 创建通知渠道
        NotificationHelper.createNotificationChannels(this)

        // 设备重启后重新调度闹钟（由 BootReceiver 处理）
    }
}

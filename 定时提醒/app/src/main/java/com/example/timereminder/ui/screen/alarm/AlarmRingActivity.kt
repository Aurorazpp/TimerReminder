package com.example.timereminder.ui.screen.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.timereminder.alarm.AlarmService
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.ui.theme.TimerReminderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 闹钟响铃全屏 Activity
 */
class AlarmRingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESCRIPTION = "description"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 解锁屏幕并保持常亮
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        val reminderId = intent?.getLongExtra(EXTRA_REMINDER_ID, -1L) ?: -1L
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "提醒"
        val description = intent?.getStringExtra(EXTRA_DESCRIPTION) ?: ""

        setContent {
            TimerReminderTheme {
                AlarmRingScreen(
                    title = title,
                    description = description,
                    onDismiss = {
                        stopAlarmService(reminderId)
                        finish()
                    },
                    onSnooze = { minutes ->
                        snoozeReminder(reminderId, title, description, minutes)
                        stopAlarmService(reminderId)
                        finish()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun stopAlarmService(reminderId: Long) {
        val intent = android.content.Intent(this, AlarmService::class.java)
        stopService(intent)
    }

    private fun snoozeReminder(
        reminderId: Long,
        title: String,
        description: String,
        minutes: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(this@AlarmRingActivity)
                val repository = ReminderRepository(db.reminderDao())
                val reminder = repository.getReminderById(reminderId)
                if (reminder != null) {
                    val snoozeEndTime = System.currentTimeMillis() + minutes * 60 * 1000L
                    val snoozed = reminder.copy(isSnoozed = true, snoozeEndTime = snoozeEndTime)
                    repository.saveReminder(snoozed)

                    // 设置贪睡闹钟
                    val scheduler = com.example.timereminder.alarm.AlarmScheduler(this@AlarmRingActivity)
                    // 创建一个临时一次性闹钟
                    val snoozeReminder = snoozed.copy(
                        type = com.example.timereminder.domain.model.ReminderType.ONCE,
                        triggerTime = snoozeEndTime,
                        isSnoozed = false,
                        snoozeEndTime = null
                    )
                    scheduler.scheduleReminder(snoozeReminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

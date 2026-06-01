package com.example.timereminder.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.timereminder.MainActivity
import com.example.timereminder.R

/**
 * 通知管理工具类
 */
object NotificationHelper {

    const val REMINDER_CHANNEL_ID = "reminder_channel"
    const val REMINDER_CHANNEL_NAME = "提醒通知"
    const val FOREGROUND_CHANNEL_ID = "foreground_service_channel"
    const val FOREGROUND_CHANNEL_NAME = "响铃服务"

    private const val REMINDER_NOTIFICATION_ID_BASE = 1000

    /**
     * 创建所有通知渠道（在 Application 初始化时调用）
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 提醒通知渠道
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            REMINDER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "定时提醒的通知"
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(reminderChannel)

        // 前台服务通知渠道
        val foregroundChannel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            FOREGROUND_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "响铃前台服务通知"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(foregroundChannel)
    }

    /**
     * 检查是否有通知权限（Android 13+）
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 发送提醒通知
     */
    fun sendReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String? = null
    ) {
        if (!hasNotificationPermission(context)) return

        // 点击通知打开主界面
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description ?: "到时间了！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        NotificationManagerCompat.from(context).notify(
            REMINDER_NOTIFICATION_ID_BASE + reminderId.toInt(),
            notification
        )
    }

    /**
     * 创建前台服务通知
     */
    fun createForegroundNotification(context: Context): android.app.Notification {
        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("响铃中...")
            .setContentText("定时提醒正在响铃")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    /**
     * 取消提醒通知
     */
    fun cancelReminderNotification(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(
            REMINDER_NOTIFICATION_ID_BASE + reminderId.toInt()
        )
    }
}

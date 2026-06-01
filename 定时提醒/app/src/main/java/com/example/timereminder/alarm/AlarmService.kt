package com.example.timereminder.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.timereminder.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 前台响铃服务
 * 播放铃声 + 震动，并显示全屏提醒 Activity
 */
class AlarmService : Service() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_RING_ENABLED = "ring_enabled"
        const val EXTRA_VIBRATION_ENABLED = "vibration_enabled"
        const val EXTRA_RINGTONE_URI = "ringtone_uri"

        /** 自动停止时间（5分钟） */
        private const val AUTO_STOP_DELAY_MS = 5 * 60 * 1000L
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var vibrationJob: Job? = null
    private var autoStopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "提醒"
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""
        val ringEnabled = intent.getBooleanExtra(EXTRA_RING_ENABLED, true)
        val vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION_ENABLED, true)
        val ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI)

        // 启动前台服务
        val notification = NotificationHelper.createForegroundNotification(this)
        startForeground(2000 + reminderId.toInt(), notification)

        // 播放铃声
        if (ringEnabled) {
            playRingtone(ringtoneUri)
        }

        // 震动
        if (vibrationEnabled) {
            startVibration()
        }

        // 显示全屏提醒 Activity
        val alarmIntent = Intent(this, com.example.timereminder.ui.screen.alarm.AlarmRingActivity::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(alarmIntent)

        // 5 分钟自动停止
        autoStopJob?.cancel()
        autoStopJob = scope.launch {
            delay(AUTO_STOP_DELAY_MS)
            stopRing()
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopRing()
        super.onDestroy()
    }

    /**
     * 停止响铃和震动
     */
    fun stopRing() {
        // 停止 MediaPlayer
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        // 停止震动
        vibrationJob?.cancel()
        vibrationJob = null
        vibrator?.cancel()

        // 取消自动停止
        autoStopJob?.cancel()
        autoStopJob = null
    }

    private fun playRingtone(ringtoneUri: String?) {
        try {
            val uri = if (ringtoneUri != null && ringtoneUri.isNotEmpty()) {
                Uri.parse(ringtoneUri)
            } else {
                // 默认闹钟铃声
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        vibrationJob?.cancel()
        vibrationJob = scope.launch {
            while (true) {
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createWaveform(
                            longArrayOf(0, 500, 500),
                            intArrayOf(0, 255, 0),
                            -1
                        )
                        it.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(longArrayOf(0, 500, 500), 0)
                    }
                }
                delay(1000)
            }
        }
    }
}

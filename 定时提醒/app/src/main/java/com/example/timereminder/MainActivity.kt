package com.example.timereminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.timereminder.alarm.AlarmScheduler
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository
import com.example.timereminder.data.repository.TagRepository
import com.example.timereminder.notification.NotificationHelper
import com.example.timereminder.ui.navigation.TimerReminderNavGraph
import com.example.timereminder.ui.theme.TimerReminderTheme

class MainActivity : ComponentActivity() {

    private lateinit var reminderRepository: ReminderRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var alarmScheduler: AlarmScheduler

    // 通知权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // 权限被拒，可引导用户去设置
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 初始化数据层
        val db = AppDatabase.getInstance(this)
        reminderRepository = ReminderRepository(db.reminderDao())
        tagRepository = TagRepository(db.tagDao())
        alarmScheduler = AlarmScheduler(this)

        // 请求通知权限
        requestNotificationPermission()

        setContent {
            TimerReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    TimerReminderNavGraph(
                        navController = navController,
                        reminderRepository = reminderRepository,
                        tagRepository = tagRepository,
                        alarmScheduler = alarmScheduler
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 已有权限
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 需要解释为什么申请权限
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

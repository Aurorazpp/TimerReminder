package com.example.timereminder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.timereminder.MainActivity
import com.example.timereminder.R
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * 定时提醒桌面小部件
 */
class ReminderWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 获取最近的提醒数据
        val reminders = runBlocking {
            try {
                val db = AppDatabase.getInstance(context)
                val repository = ReminderRepository(db.reminderDao())
                repository.getRecentReminders(5)
            } catch (e: Exception) {
                emptyList()
            }
        }

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, reminders)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        reminders: List<com.example.timereminder.domain.model.Reminder>
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_reminder)

        // 点击小部件打开主界面
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

        if (reminders.isEmpty()) {
            views.setViewVisibility(R.id.widget_list, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_empty, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_list, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widget_empty, android.view.View.GONE)

            // 构建列表条目
            try {
                val remoteViews = RemoteViews(context.packageName, R.layout.layout_widget_item)
                // 使用 setRemoteAdapter 填充列表
                val adapterIntent = Intent(context, WidgetRemoteViewsService::class.java)
                views.setRemoteAdapter(R.id.widget_list, adapterIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

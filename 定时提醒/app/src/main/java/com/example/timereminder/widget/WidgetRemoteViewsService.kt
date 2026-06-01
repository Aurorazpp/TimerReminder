package com.example.timereminder.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.timereminder.R
import com.example.timereminder.data.db.AppDatabase
import com.example.timereminder.data.repository.ReminderRepository

/**
 * 桌面小部件列表数据适配器
 */
class WidgetRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetRemoteViewsFactory(applicationContext)
    }
}

class WidgetRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val reminders = mutableListOf<com.example.timereminder.domain.model.Reminder>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // 重新加载数据
        reminders.clear()
        try {
            val db = AppDatabase.getInstance(context)
            val repository = ReminderRepository(db.reminderDao())
            val loaded = kotlinx.coroutines.runBlocking {
                repository.getRecentReminders(10)
            }
            reminders.addAll(loaded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        reminders.clear()
    }

    override fun getCount(): Int = reminders.size

    override fun getViewAt(position: Int): RemoteViews {
        val reminder = reminders[position]
        val views = RemoteViews(context.packageName, R.layout.layout_widget_item)

        val timeStr = "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}"
        views.setTextViewText(R.id.widget_item_time, timeStr)
        views.setTextViewText(R.id.widget_item_title, reminder.title)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = reminders[position].id

    override fun hasStableIds(): Boolean = true
}

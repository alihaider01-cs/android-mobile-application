package com.example.taskmanager.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.taskmanager.MainActivity
import com.example.taskmanager.R
import com.example.taskmanager.data.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.task_widget)

            // Get tasks from SharedPreferences
            val prefs = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("tasks_list", null)
            val gson = Gson()
            
            val tasks: List<Task> = if (json != null) {
                val type = object : TypeToken<List<Task>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }

            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.isCompleted }
            
            // Update Progress
            views.setTextViewText(R.id.widget_progress_text, "$completedTasks/$totalTasks")
            if (totalTasks > 0) {
                views.setProgressBar(R.id.widget_progress_bar, totalTasks, completedTasks, false)
            } else {
                views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
            }

            // Update Task List (first 3-4 tasks)
            val taskListBuilder = StringBuilder()
            if (tasks.isEmpty()) {
                taskListBuilder.append("No tasks found")
            } else {
                tasks.take(4).forEach { task ->
                    val status = if (task.isCompleted) "✓ " else "○ "
                    taskListBuilder.append(status).append(task.name).append("\n")
                }
                if (tasks.size > 4) {
                    taskListBuilder.append("...")
                }
            }
            views.setTextViewText(R.id.widget_task_list, taskListBuilder.toString())

            // Intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

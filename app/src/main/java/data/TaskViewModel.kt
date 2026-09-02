package com.example.taskmanager.data

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.utils.AchievementManager
import com.example.taskmanager.utils.TaskWidgetProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
    private val achievementManager = AchievementManager(application)
    private val gson = Gson()

    private val _tasks = MutableLiveData<MutableList<Task>>()
    val tasks: LiveData<MutableList<Task>> = _tasks

    private val _newAchievement = MutableLiveData<Achievement?>()
    val newAchievement: LiveData<Achievement?> = _newAchievement

    fun clearAchievement() {
        _newAchievement.value = null
    }

    fun getAchievements(): List<Achievement> {
        return achievementManager.getAchievements()
    }

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val tasksList = withContext(Dispatchers.IO) {
                val json = sharedPreferences.getString("tasks_list", null)
                if (json != null) {
                    val type = object : TypeToken<MutableList<Task>>() {}.type
                    try {
                        gson.fromJson<MutableList<Task>>(json, type)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

            if (tasksList != null) {
                _tasks.value = tasksList!!
            } else {
                // Initial sample tasks
                val sampleTasks = mutableListOf(
                    Task(name = "Buy groceries", priority = 2, category = "Shopping"),
                    Task(name = "Call dentist", priority = 1, category = "Personal"),
                    Task(name = "Complete project", priority = 1, category = "Work")
                )
                _tasks.value = sampleTasks
                saveTasksToPrefs(sampleTasks)
            }
        }
    }

    private fun saveTasksToPrefs(list: List<Task>) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = gson.toJson(list)
            sharedPreferences.edit().putString("tasks_list", json).apply()
            withContext(Dispatchers.Main) {
                updateWidget()
            }
        }
    }

    private fun updateWidget() {
        val intent = Intent(getApplication(), TaskWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(getApplication())
            .getAppWidgetIds(ComponentName(getApplication(), TaskWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        getApplication<Application>().sendBroadcast(intent)
    }

    fun addTask(task: Task) {
        val currentList = _tasks.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, task)
        _tasks.value = currentList
        saveTasksToPrefs(currentList)
        checkAchievements(currentList)
    }

    fun updateTask(task: Task) {
        val currentList = _tasks.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index != -1) {
            val oldTask = currentList[index]
            if (!oldTask.isCompleted && task.isCompleted) {
                task.completedAt = System.currentTimeMillis()
            } else if (!task.isCompleted) {
                task.completedAt = null
            }
            
            currentList[index] = task
            _tasks.value = currentList
            saveTasksToPrefs(currentList)
            checkAchievements(currentList)
        }
    }

    private fun checkAchievements(tasks: List<Task>) {
        viewModelScope.launch {
            val newlyEarned = withContext(Dispatchers.Default) {
                achievementManager.checkAchievements(tasks)
            }
            if (newlyEarned.isNotEmpty()) {
                _newAchievement.value = newlyEarned.first()
            }
        }
    }

    fun deleteTask(task: Task) {
        val currentList = _tasks.value?.toMutableList() ?: return
        currentList.removeAll { it.id == task.id }
        _tasks.value = currentList
        saveTasksToPrefs(currentList)
    }
}

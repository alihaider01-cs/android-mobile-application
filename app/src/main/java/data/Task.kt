package com.example.taskmanager.data

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val priority: Int = 2,
    val category: String = "General",
    var isCompleted: Boolean = false,
    var completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: Long? = null
) {
    fun getPriorityText(): String {
        return when (priority) {
            1 -> "High"
            2 -> "Medium"
            else -> "Low"
        }
    }
}
package com.example.taskmanager.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    var isEarned: Boolean = false,
    val earnedAt: Long? = null
)

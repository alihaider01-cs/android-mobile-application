package com.example.taskmanager.utils

import android.content.Context
import com.example.taskmanager.data.Achievement
import com.example.taskmanager.data.Task
import java.util.*

class AchievementManager(context: Context) {
    private val prefs = context.getSharedPreferences("achievements_prefs", Context.MODE_PRIVATE)

    private val _achievements = listOf(
        Achievement("early_bird", "Early Bird", "Complete task before 9 AM", "🥇"),
        Achievement("night_owl", "Night Owl", "Complete task after 10 PM", "🦉"),
        Achievement("streak_7", "7-Day Streak", "Complete tasks for 7 days", "🔥"),
        Achievement("marathon", "Marathon", "Complete 50 tasks total", "💪"),
        Achievement("speed_demon", "Speed Demon", "Add 10 tasks in 1 minute", "⚡"),
        Achievement("perfect_week", "Perfect Week", "Complete all tasks for a week", "📅")
    )

    fun getAchievements(): List<Achievement> {
        val earnedIds = prefs.getStringSet("earned_badges", emptySet()) ?: emptySet()
        return _achievements.map { 
            it.copy(isEarned = earnedIds.contains(it.id))
        }
    }

    fun checkAchievements(tasks: List<Task>): List<Achievement> {
        val earnedIds = prefs.getStringSet("earned_badges", emptySet())?.toMutableSet() ?: mutableSetOf()
        val newlyEarned = mutableListOf<Achievement>()

        // 1. Early Bird: Complete task before 9 AM
        if (!earnedIds.contains("early_bird")) {
            val hasEarlyBird = tasks.any { it.isCompleted && it.completedAt != null && isBefore9AM(it.completedAt!!) }
            if (hasEarlyBird) markEarned("early_bird", earnedIds, newlyEarned)
        }

        // 2. Night Owl: Complete task after 10 PM
        if (!earnedIds.contains("night_owl")) {
            val hasNightOwl = tasks.any { it.isCompleted && it.completedAt != null && isAfter10PM(it.completedAt!!) }
            if (hasNightOwl) markEarned("night_owl", earnedIds, newlyEarned)
        }

        // 3. Marathon: Complete 50 tasks total
        if (!earnedIds.contains("marathon")) {
            if (tasks.count { it.isCompleted } >= 50) {
                markEarned("marathon", earnedIds, newlyEarned)
            }
        }

        // 4. Speed Demon: Add 10 tasks in 1 minute
        if (!earnedIds.contains("speed_demon")) {
            val sortedTasks = tasks.sortedByDescending { it.createdAt }
            if (sortedTasks.size >= 10) {
                for (i in 0..sortedTasks.size - 10) {
                    if (sortedTasks[i].createdAt - sortedTasks[i + 9].createdAt <= 60000) {
                        markEarned("speed_demon", earnedIds, newlyEarned)
                        break
                    }
                }
            }
        }

        // 5. 7-Day Streak
        if (!earnedIds.contains("streak_7")) {
            if (checkStreak(tasks, 7)) {
                markEarned("streak_7", earnedIds, newlyEarned)
            }
        }

        // 6. Perfect Week
        if (!earnedIds.contains("perfect_week")) {
            if (checkPerfectWeek(tasks)) {
                markEarned("perfect_week", earnedIds, newlyEarned)
            }
        }

        if (newlyEarned.isNotEmpty()) {
            prefs.edit().putStringSet("earned_badges", earnedIds).apply()
        }

        return newlyEarned
    }

    private fun markEarned(id: String, earnedIds: MutableSet<String>, newlyEarned: MutableList<Achievement>) {
        earnedIds.add(id)
        _achievements.find { it.id == id }?.let { newlyEarned.add(it) }
    }

    private fun isBefore9AM(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.HOUR_OF_DAY) < 9
    }

    private fun isAfter10PM(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.HOUR_OF_DAY) >= 22
    }

    private fun checkStreak(tasks: List<Task>, days: Int): Boolean {
        val completedDates = tasks.filter { it.isCompleted && it.completedAt != null }
            .map { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.completedAt!!
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.distinct().sortedDescending()

        if (completedDates.size < days) return false

        var currentStreak = 1
        for (i in 0 until completedDates.size - 1) {
            val diff = completedDates[i] - completedDates[i+1]
            if (diff <= 86400000L + 3600000L) { // 1 day + 1 hour buffer for DST etc
                currentStreak++
                if (currentStreak >= days) return true
            } else {
                currentStreak = 1
            }
        }
        return false
    }

    private fun checkPerfectWeek(tasks: List<Task>): Boolean {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        
        val tasksInLast7Days = tasks.filter { it.createdAt >= sevenDaysAgo }
        if (tasksInLast7Days.isEmpty()) return false
        
        return tasksInLast7Days.all { it.isCompleted }
    }
}

package com.example.taskmanager.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.taskmanager.R

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"

    const val THEME_DEFAULT = 0
    const val THEME_OCEAN = 1
    const val THEME_SUNSET = 2
    const val THEME_FOREST = 3
    const val THEME_PURPLE = 4
    const val THEME_DARK = 5

    fun saveTheme(context: Context, themeIndex: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME, themeIndex).apply()
    }

    fun getThemeIndex(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME, THEME_DEFAULT)
    }

    fun applyTheme(context: Context) {
        when (getThemeIndex(context)) {
            THEME_OCEAN -> context.setTheme(R.style.Theme_TaskManager_Ocean)
            THEME_SUNSET -> context.setTheme(R.style.Theme_TaskManager_Sunset)
            THEME_FOREST -> context.setTheme(R.style.Theme_TaskManager_Forest)
            THEME_PURPLE -> context.setTheme(R.style.Theme_TaskManager_Purple)
            THEME_DARK -> context.setTheme(R.style.Theme_TaskManager_Dark)
            else -> context.setTheme(R.style.Theme_TaskManager)
        }
    }
}

package com.darkstar.wallora.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.darkstar.wallora.model.ThemeMode

class PreferencesStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(readThemeMode())
        private set

    var dynamicColors by mutableStateOf(preferences.getBoolean(KEY_DYNAMIC_COLORS, true))
        private set

    fun updateThemeMode(value: ThemeMode) {
        themeMode = value
        preferences.edit().putString(KEY_THEME, value.name).apply()
    }

    fun updateDynamicColors(value: Boolean) {
        dynamicColors = value
        preferences.edit().putBoolean(KEY_DYNAMIC_COLORS, value).apply()
    }

    private fun readThemeMode(): ThemeMode = runCatching {
        ThemeMode.valueOf(preferences.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    }.getOrDefault(ThemeMode.SYSTEM)

    companion object {
        private const val PREFERENCES_NAME = "wallora_preferences"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_DYNAMIC_COLORS = "dynamic_colors"
    }
}

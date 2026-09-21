package com.darkstar.wallora.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoriteStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _favoriteIds = MutableStateFlow(readIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_IDS) _favoriteIds.value = readIds()
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun contains(id: String): Boolean = _favoriteIds.value.contains(id)

    fun toggle(id: String) {
        val next = _favoriteIds.value.toMutableSet()
        if (!next.add(id)) next.remove(id)
        preferences.edit { putStringSet(KEY_IDS, next) }
        _favoriteIds.value = next
    }

    private fun readIds(): Set<String> =
        preferences.getStringSet(KEY_IDS, emptySet()).orEmpty().toSet()

    companion object {
        private const val PREFERENCES_NAME = "wallora_preferences"
        private const val KEY_IDS = "favorite_ids"
    }
}

package com.darkstar.wallora.data

import android.content.Context

class FavoriteStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun contains(id: String): Boolean = preferences.getStringSet(KEY_IDS, emptySet()).orEmpty().contains(id)

    fun ids(): Set<String> = preferences.getStringSet(KEY_IDS, emptySet()).orEmpty()

    fun toggle(id: String) {
        val next = ids().toMutableSet()
        if (!next.add(id)) next.remove(id)
        preferences.edit().putStringSet(KEY_IDS, next).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "wallora_preferences"
        private const val KEY_IDS = "favorite_ids"
    }
}

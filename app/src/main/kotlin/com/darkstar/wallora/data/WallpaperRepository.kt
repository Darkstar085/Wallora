package com.darkstar.wallora.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.darkstar.wallora.model.Wallpaper

class WallpaperRepository(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun getWallpapers(): Result<List<Wallpaper>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(API_URL).build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Wallpaper service returned HTTP ${response.code}" }
                val body = response.body?.string()?.takeIf { it.isNotBlank() }
                    ?: error("Wallpaper service returned an empty response")
                parse(body)
            }
        }
    }

    private fun parse(body: String): List<Wallpaper> {
        val items = JSONObject(body).getJSONArray("wallpapers")
        return buildList(items.length()) {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                add(
                    Wallpaper(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        category = item.getString("category"),
                        width = item.getInt("width"),
                        height = item.getInt("height"),
                        format = item.getString("format"),
                        path = item.getString("path"),
                        url = item.getString("url"),
                    ),
                )
            }
        }
    }

    companion object {
        const val API_URL = "https://raw.githubusercontent.com/Darkstar085/Wallpapers/main/api/wallpapers.json"
    }
}

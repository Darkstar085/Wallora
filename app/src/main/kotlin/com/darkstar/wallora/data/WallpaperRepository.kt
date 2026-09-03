package com.darkstar.wallora.data

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.darkstar.wallora.model.Wallpaper

class WallpaperRepository(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient(),
) {
    private var cachedWallpapers: List<Wallpaper>? = null

    suspend fun getWallpapers(): Result<List<Wallpaper>> = withContext(Dispatchers.IO) {
        cachedWallpapers?.let { return@withContext Result.success(it) }
        try {
            val request = Request.Builder().url(API_URL).build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Wallpaper service returned HTTP ${response.code}" }
                val body = response.body?.string()?.takeIf { it.isNotBlank() }
                    ?: error("Wallpaper service returned an empty response")
                val wallpapers = parse(body).shuffled()
                cacheFile.writeText(body)
                cachedWallpapers = wallpapers
                Result.success(wallpapers)
            }
        } catch (networkError: Exception) {
            readCachedWallpapers()?.let { cachedWallpapers = it.shuffled(); Result.success(cachedWallpapers!!) }
                ?: Result.failure(networkError)
        }
    }

    private fun readCachedWallpapers(): List<Wallpaper>? = runCatching {
        val body = cacheFile.takeIf { it.isFile }?.readText()?.takeIf { it.isNotBlank() }
            ?: return@runCatching null
        parse(body)
    }.getOrNull()

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
                        filename = item.optString("filename").ifBlank { item.getString("path").substringAfterLast('/') },
                        fileSizeBytes = item.optLong("file_size_bytes", 0L),
                        addedAt = item.optString("added_at").takeIf { it.isNotBlank() },
                    ),
                )
            }
        }
    }

    private val cacheFile: File
        get() = File(context.filesDir, CACHE_FILE_NAME)

    companion object {
        const val API_URL = "https://raw.githubusercontent.com/Darkstar085/Wallpapers/main/api/wallpapers.json"
        private const val CACHE_FILE_NAME = "wallpapers.json"
    }
}

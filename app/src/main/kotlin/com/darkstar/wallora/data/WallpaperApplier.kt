package com.darkstar.wallora.data

import android.app.WallpaperManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class WallpaperApplier(private val context: Context) {
    suspend fun apply(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = true
            connection.connect()
            try {
                check(connection.responseCode in 200..299) { "Image download returned HTTP ${connection.responseCode}" }
                val bytes = connection.inputStream.use { it.readBytes() }
                check(bytes.isNotEmpty()) { "Downloaded image is empty" }
                WallpaperManager.getInstance(context).setStream(ByteArrayInputStream(bytes))
                Unit
            } finally {
                connection.disconnect()
            }
        }
    }
}

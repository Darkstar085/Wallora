package com.darkstar.wallora.data

import android.app.WallpaperManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import com.darkstar.wallora.model.WallpaperTarget

class WallpaperApplier(private val context: Context) {
    suspend fun apply(url: String, target: WallpaperTarget = WallpaperTarget.BOTH): Result<Unit> = withContext(Dispatchers.IO) {
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
                val manager = WallpaperManager.getInstance(context)
                ByteArrayInputStream(bytes).use { stream ->
                    val flags = when (target) {
                        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    manager.setStream(stream, null, true, flags)
                    Unit
                }
            } finally {
                connection.disconnect()
            }
        }
    }
}

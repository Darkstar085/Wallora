package com.darkstar.wallora.data

import android.app.WallpaperManager
import android.content.Context
import com.darkstar.wallora.model.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class WallpaperApplier(
    private val context: Context,
    private val client: OkHttpClient = NetworkClient.client,
) {
    suspend fun apply(url: String, target: WallpaperTarget = WallpaperTarget.BOTH): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(Request.Builder().url(url).build()).execute().use { call ->
                check(call.isSuccessful) { "Image download returned HTTP ${call.code}" }
                call.body?.byteStream()?.use { stream ->
                    val flags = when (target) {
                        WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    WallpaperManager.getInstance(context).setStream(stream, null, true, flags)
                    Unit
                } ?: error("Image download returned an empty response")
            }
        }
    }
}

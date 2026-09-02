package com.darkstar.wallora.data

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import java.util.Locale
import okio.Path.Companion.toPath

class ImageCacheManager(
    private val context: Context,
) {
    private val diskCache: DiskCache by lazy {
        DiskCache.Builder()
            .directory(context.filesDir.resolve(CACHE_DIRECTORY).absolutePath.toPath())
            .maxSizeBytes(MAX_CACHE_SIZE_BYTES)
            .build()
    }

    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .diskCache(diskCache)
            .build()
    }

    fun cacheSizeBytes(): Long = diskCache.size + metadataCacheSize()

    fun clear() {
        imageLoader.memoryCache?.clear()
        diskCache.clear()
        context.filesDir.resolve(METADATA_CACHE_FILE).delete()
    }

    fun imageRequest(url: String): ImageRequest = ImageRequest.Builder(context)
        .data(url)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()

    fun formattedSize(): String = formatBytes(cacheSizeBytes())

    private fun metadataCacheSize(): Long = context.filesDir.resolve(METADATA_CACHE_FILE).length()

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = -1
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return String.format(Locale.US, "%.1f %s", value, units[unitIndex])
    }

    companion object {
        private const val CACHE_DIRECTORY = "wallpaper_image_cache"
        private const val METADATA_CACHE_FILE = "wallpapers.json"
        private const val MAX_CACHE_SIZE_BYTES = 500L * 1024 * 1024
    }
}

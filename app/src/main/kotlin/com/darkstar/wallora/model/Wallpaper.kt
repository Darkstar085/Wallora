package com.darkstar.wallora.model

data class Wallpaper(
    val id: String,
    val title: String,
    val category: String,
    val width: Int,
    val height: Int,
    val format: String,
    val path: String,
    val url: String,
    val filename: String = path.substringAfterLast('/'),
    val fileSizeBytes: Long = 0L,
    val addedAt: String? = null,
)

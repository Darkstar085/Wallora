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
)

package com.darkstar.wallora.model

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class WallpaperTarget(val label: String) {
    HOME("Home screen"),
    LOCK("Lock screen"),
    BOTH("Home & lock screen"),
}

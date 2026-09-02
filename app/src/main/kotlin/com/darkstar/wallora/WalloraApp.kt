package com.darkstar.wallora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.home.HomeScreen
import com.darkstar.wallora.ui.theme.WalloraTheme

private enum class AppTab(val label: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    FAVORITES("Favorites"),
    SETTINGS("Settings"),
}

@Composable
fun WalloraApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember { WallpaperRepository() }
    val favorites = remember { FavoriteStore(context) }
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    if (selectedWallpaper != null) {
        com.darkstar.wallora.ui.preview.WallpaperPreviewScreen(
            wallpaper = selectedWallpaper!!,
            isFavorite = favorites.contains(selectedWallpaper!!.id),
            onBack = { selectedWallpaper = null },
            onToggleFavorite = { favorites.toggle(selectedWallpaper!!.id) },
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AppTab.HOME -> Icons.Outlined.Home
                                    AppTab.EXPLORE -> Icons.Outlined.Explore
                                    AppTab.FAVORITES -> Icons.Outlined.FavoriteBorder
                                    AppTab.SETTINGS -> Icons.Outlined.Settings
                                },
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(repository, favorites, padding) { selectedWallpaper = it }
                AppTab.EXPLORE -> com.darkstar.wallora.ui.explore.ExploreScreen(repository, favorites, padding) { selectedWallpaper = it }
                AppTab.FAVORITES -> com.darkstar.wallora.ui.favorites.FavoritesScreen(repository, favorites, padding) { selectedWallpaper = it }
                AppTab.SETTINGS -> com.darkstar.wallora.ui.settings.SettingsScreen(padding)
            }
        }
    }
}

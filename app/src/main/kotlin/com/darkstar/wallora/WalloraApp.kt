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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.explore.ExploreScreen
import com.darkstar.wallora.ui.favorites.FavoritesScreen
import com.darkstar.wallora.ui.home.HomeScreen
import com.darkstar.wallora.ui.preview.WallpaperPreviewScreen
import com.darkstar.wallora.ui.settings.SettingsScreen

private enum class AppTab(val label: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    FAVORITES("Favorites"),
    SETTINGS("Settings"),
}

@Composable
fun WalloraApp(preferences: PreferencesStore) {
    val context = LocalContext.current.applicationContext
    val repository = remember { WallpaperRepository() }
    val favorites = remember { FavoriteStore(context) }
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    if (selectedWallpaper != null) {
        WallpaperPreviewScreen(
            wallpaper = selectedWallpaper!!,
            isFavorite = favorites.contains(selectedWallpaper!!.id),
            wallpaperTarget = preferences.wallpaperTarget,
            onBack = { selectedWallpaper = null },
            onToggleFavorite = { favorites.toggle(selectedWallpaper!!.id) },
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 0.dp) {
                AppTab.entries.forEach { tab ->
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
                        colors = NavigationBarItemDefaults.colors(),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(repository, favorites, padding) { selectedWallpaper = it }
                AppTab.EXPLORE -> ExploreScreen(repository, favorites, padding) { selectedWallpaper = it }
                AppTab.FAVORITES -> FavoritesScreen(repository, favorites, padding) { selectedWallpaper = it }
                AppTab.SETTINGS -> SettingsScreen(padding, preferences)
            }
        }
    }
}

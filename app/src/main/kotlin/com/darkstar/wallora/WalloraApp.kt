package com.darkstar.wallora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.ImageCacheManager
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.components.LocalImageCacheManager
import com.darkstar.wallora.ui.favorites.FavoritesScreen
import com.darkstar.wallora.ui.home.HomeScreen
import com.darkstar.wallora.ui.preview.WallpaperPreviewScreen
import com.darkstar.wallora.ui.settings.SettingsScreen

private enum class AppTab(val label: String) {
    HOME("Home"),
    FAVORITES("Favorites"),
    SETTINGS("Settings"),
}

@Composable
fun WalloraApp(preferences: PreferencesStore) {
    val context = LocalContext.current.applicationContext
    val repository = remember { WallpaperRepository(context) }
    val favorites = remember { FavoriteStore(context) }
    val imageCache = remember { ImageCacheManager(context) }
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var selectedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

    CompositionLocalProvider(LocalImageCacheManager provides imageCache) {
        if (selectedWallpaper != null) {
            WallpaperPreviewScreen(
                wallpaper = selectedWallpaper!!,
                isFavorite = favorites.contains(selectedWallpaper!!.id),
                wallpaperTarget = preferences.wallpaperTarget,
                onBack = { selectedWallpaper = null },
                onToggleFavorite = { favorites.toggle(selectedWallpaper!!.id) },
            )
            return@CompositionLocalProvider
        }

        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(34.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
                    tonalElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            Surface(
                                onClick = { selectedTab = tab },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(28.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    Color.Transparent
                                },
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Icon(
                                        imageVector = when (tab) {
                                            AppTab.HOME -> Icons.Outlined.Home
                                            AppTab.FAVORITES -> Icons.Outlined.FavoriteBorder
                                            AppTab.SETTINGS -> Icons.Outlined.Settings
                                        },
                                        contentDescription = tab.label,
                                        tint = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(25.dp),
                                    )
                                    Text(
                                        text = tab.label,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                when (selectedTab) {
                    AppTab.HOME -> HomeScreen(repository, favorites, padding) { selectedWallpaper = it }
                    AppTab.FAVORITES -> FavoritesScreen(repository, favorites, padding) { selectedWallpaper = it }
                    AppTab.SETTINGS -> SettingsScreen(padding, preferences, imageCache)
                }
            }
        }
    }
}

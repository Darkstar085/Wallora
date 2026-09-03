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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.darkstar.wallora.R
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

private enum class AppTab { HOME, FAVORITES, SETTINGS;
    @Composable fun label(): String = when (this) { HOME -> stringResource(R.string.tab_home); FAVORITES -> stringResource(R.string.tab_favorites); SETTINGS -> stringResource(R.string.tab_settings) }
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
            WallpaperPreviewScreen(selectedWallpaper!!, favorites.contains(selectedWallpaper!!.id), preferences, repository, { selectedWallpaper = null }, { id -> favorites.toggle(id) })
            return@CompositionLocalProvider
        }
        Scaffold(bottomBar = {
            Surface(Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.bottom_bar_horizontal_padding), vertical = dimensionResource(R.dimen.bottom_bar_vertical_padding)), shape = RoundedCornerShape(dimensionResource(R.dimen.bottom_bar_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f), tonalElevation = dimensionResource(R.dimen.bottom_bar_elevation)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.bottom_bar_content_horizontal_padding), vertical = dimensionResource(R.dimen.bottom_bar_content_vertical_padding)), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    AppTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        Surface(onClick = { selectedTab = tab }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(dimensionResource(R.dimen.bottom_bar_item_corner_radius)), color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent) {
                            Column(Modifier.padding(vertical = dimensionResource(R.dimen.bottom_bar_item_vertical_padding)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.bottom_bar_item_spacing))) {
                                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                Icon(when (tab) { AppTab.HOME -> Icons.Outlined.Home; AppTab.FAVORITES -> Icons.Outlined.FavoriteBorder; AppTab.SETTINGS -> Icons.Outlined.Settings }, contentDescription = tab.label(), tint = tint, modifier = Modifier.size(dimensionResource(R.dimen.bottom_bar_icon_size)))
                                Text(tab.label(), color = tint, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }) { padding ->
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

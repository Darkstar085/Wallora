package com.darkstar.wallora.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.components.WallpaperCard

@Composable
fun FavoritesScreen(
    repository: WallpaperRepository,
    favoriteStore: FavoriteStore,
    contentPadding: PaddingValues,
    onWallpaperClick: (Wallpaper) -> Unit,
) {
    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getWallpapers().onSuccess { wallpapers = it; loading = false }.onFailure { loading = false }
    }

    val favorites = wallpapers.filter { it.id in favoriteStore.ids() }

    if (loading) {
        Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(
                start = 18.dp,
                top = contentPadding.calculateTopPadding() + 24.dp,
                end = 18.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
        ) {
            Text("Favorites", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Your saved wallpapers", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No favorites yet", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Tap the heart on a wallpaper to save it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = contentPadding.calculateTopPadding() + 24.dp, end = 18.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.padding(bottom = 6.dp)) {
                Text("Favorites", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Your saved wallpapers", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(favorites) { wallpaper -> WallpaperCard(wallpaper, favoriteStore) { onWallpaperClick(wallpaper) } }
    }
}

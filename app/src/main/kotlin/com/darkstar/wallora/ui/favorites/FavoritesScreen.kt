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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.darkstar.wallora.R
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
        Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(
                start = dimensionResource(R.dimen.screen_horizontal_padding),
                top = contentPadding.calculateTopPadding() + dimensionResource(R.dimen.screen_vertical_padding),
                end = dimensionResource(R.dimen.screen_horizontal_padding),
                bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.screen_vertical_padding),
            ),
        ) {
            Text(stringResource(R.string.tab_favorites), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.favorites_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.no_favorites_yet), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(dimensionResource(R.dimen.tiny_spacing)))
                    Text(stringResource(R.string.favorite_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimensionResource(R.dimen.screen_horizontal_padding),
            top = contentPadding.calculateTopPadding() + dimensionResource(R.dimen.screen_vertical_padding),
            end = dimensionResource(R.dimen.screen_horizontal_padding),
            bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.screen_vertical_padding),
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_horizontal_spacing)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_vertical_spacing)),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.padding(bottom = dimensionResource(R.dimen.small_spacing))) {
                Text(stringResource(R.string.tab_favorites), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.favorites_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(favorites) { wallpaper -> WallpaperCard(wallpaper, favoriteStore) { onWallpaperClick(wallpaper) } }
    }
}

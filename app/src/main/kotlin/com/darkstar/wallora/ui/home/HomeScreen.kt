package com.darkstar.wallora.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.darkstar.wallora.R
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.components.WallpaperCard
import com.darkstar.wallora.ui.components.WallpaperImage
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(repository: WallpaperRepository, favoriteStore: FavoriteStore, contentPadding: PaddingValues, onWallpaperClick: (Wallpaper) -> Unit) {
    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }
    val loadErrorFallback = stringResource(R.string.unable_to_load_wallpapers)
    val loadingPlaceholderCount = integerResource(R.integer.loading_placeholder_count)

    fun load() { loading = true; error = null }
    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        repository.getWallpapers().onSuccess { wallpapers = it; loading = false }.onFailure { error = it.message ?: loadErrorFallback; loading = false }
    }

    val categories = remember(wallpapers) { listOf("All") + wallpapers.map { it.category }.distinct().sorted() }
    val filtered = wallpapers.filter { selectedCategory == "All" || it.category == selectedCategory }
    val randomized = remember(wallpapers) { wallpapers.shuffled() }
    val displayedWallpapers = if (selectedCategory == "All") randomized else filtered

    LazyVerticalGrid(
        columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = dimensionResource(R.dimen.screen_horizontal_padding), top = contentPadding.calculateTopPadding() + dimensionResource(R.dimen.home_top_spacing), end = dimensionResource(R.dimen.screen_horizontal_padding), bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.screen_vertical_padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_horizontal_spacing)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.grid_vertical_spacing)),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.brand_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(dimensionResource(R.dimen.section_spacing)))
                when {
                    loading -> LoadingPanel()
                    error != null -> ErrorPanel(error!!, ::load)
                    displayedWallpapers.isNotEmpty() -> FeaturedCarousel(displayedWallpapers, favoriteStore, onWallpaperClick)
                    else -> EmptyPanel(stringResource(R.string.no_wallpapers_found))
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.section_spacing)))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.collections), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.weight(1f))
                    Text("${categories.size - 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.tiny_spacing)))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tiny_spacing))) {
                    lazyItems(categories) { category ->
                        Surface(onClick = { selectedCategory = category }, shape = RoundedCornerShape(dimensionResource(R.dimen.category_corner_radius)), color = if (selectedCategory == category) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (selectedCategory == category) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) {
                            Text(category, Modifier.padding(horizontal = dimensionResource(R.dimen.chip_horizontal_padding), vertical = dimensionResource(R.dimen.chip_vertical_padding)))
                        }
                    }
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.section_spacing)))
                Text(stringResource(R.string.wallpapers), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(dimensionResource(R.dimen.small_spacing)))
            }
        }
        if (loading) items(loadingPlaceholderCount) { LoadingGridItem() } else items(displayedWallpapers) { wallpaper -> WallpaperCard(wallpaper, favoriteStore) { onWallpaperClick(wallpaper) } }
    }
}

@Composable
private fun FeaturedCarousel(wallpapers: List<Wallpaper>, favoriteStore: FavoriteStore, onWallpaperClick: (Wallpaper) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { wallpapers.size })
    val autoScrollDelay = integerResource(R.integer.featured_auto_scroll_delay_ms).toLong()
    LaunchedEffect(pagerState, wallpapers.size, autoScrollDelay) {
        while (wallpapers.size > 1) {
            delay(autoScrollDelay)
            if (!pagerState.isScrollInProgress) pagerState.animateScrollToPage((pagerState.currentPage + 1) % wallpapers.size)
        }
    }
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.featured_height)), pageSpacing = dimensionResource(R.dimen.pager_page_spacing)) { page ->
        val wallpaper = wallpapers[page]
        var favorite by remember(wallpaper.id) { mutableStateOf(favoriteStore.contains(wallpaper.id)) }
        Surface(onClick = { onWallpaperClick(wallpaper) }, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(dimensionResource(R.dimen.featured_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box {
                WallpaperImage(wallpaper, Modifier.fillMaxSize(), ContentScale.Crop)
                IconButton(onClick = { favorite = !favorite; favoriteStore.toggle(wallpaper.id) }, modifier = Modifier.align(Alignment.TopEnd).padding(dimensionResource(R.dimen.favorite_button_padding)).background(colorResource(R.color.favorite_scrim), CircleShape)) {
                    Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = stringResource(if (favorite) R.string.remove_from_favorites else R.string.add_to_favorites), tint = colorResource(R.color.preview_text))
                }
            }
        }
    }
}

@Composable
private fun LoadingPanel() {
    Surface(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.featured_height)), shape = RoundedCornerShape(dimensionResource(R.dimen.featured_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
}

@Composable
private fun LoadingGridItem() {
    Surface(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.card_height)), shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = dimensionResource(R.dimen.progress_stroke_width)) } }
}

@Composable
private fun ErrorPanel(message: String, retry: () -> Unit) {
    Surface(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.panel_height)), shape = RoundedCornerShape(dimensionResource(R.dimen.panel_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.preview_content_padding)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.couldnt_load_wallpapers), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(dimensionResource(R.dimen.tiny_spacing)))
            Button(onClick = retry) { Icon(Icons.Outlined.Refresh, contentDescription = null); Spacer(Modifier.size(dimensionResource(R.dimen.tiny_spacing))); Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
private fun EmptyPanel(message: String) {
    Surface(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.panel_height)), shape = RoundedCornerShape(dimensionResource(R.dimen.panel_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.components.WallpaperCard
import com.darkstar.wallora.ui.components.WallpaperImage
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    repository: WallpaperRepository,
    favoriteStore: FavoriteStore,
    contentPadding: PaddingValues,
    onWallpaperClick: (Wallpaper) -> Unit,
) {
    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    fun load() {
        loading = true
        error = null
    }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        repository.getWallpapers().onSuccess {
            wallpapers = it
            loading = false
        }.onFailure {
            error = it.message ?: "Unable to load wallpapers"
            loading = false
        }
    }

    val categories = remember(wallpapers) { listOf("All") + wallpapers.map { it.category }.distinct().sorted() }
    val filtered = wallpapers.filter { selectedCategory == "All" || it.category == selectedCategory }
    val randomized = remember(wallpapers) { wallpapers.shuffled() }
    val displayedWallpapers = if (selectedCategory == "All") randomized else filtered

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 18.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Wallora",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Text(
                            "Minimal wallpapers, beautifully presented.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                when {
                    loading -> LoadingPanel()
                    error != null -> ErrorPanel(error!!, ::load)
                    displayedWallpapers.isNotEmpty() -> FeaturedCarousel(displayedWallpapers, favoriteStore, onWallpaperClick)
                    else -> EmptyPanel("No wallpapers found")
                }
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Collections",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.weight(1f))
                    Text("${categories.size - 1}", color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lazyItems(categories) { category ->
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(50),
                            color = if (selectedCategory == category) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedCategory == category) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        ) {
                            Text(category, modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp))
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "Wallpapers",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        if (loading) {
            items(6) { LoadingGridItem() }
        } else {
            items(displayedWallpapers) { wallpaper -> WallpaperCard(wallpaper, favoriteStore) { onWallpaperClick(wallpaper) } }
        }
    }
}

@Composable
private fun FeaturedCarousel(
    wallpapers: List<Wallpaper>,
    favoriteStore: FavoriteStore,
    onWallpaperClick: (Wallpaper) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { wallpapers.size })

    LaunchedEffect(pagerState, wallpapers.size) {
        while (wallpapers.size > 1) {
            delay(4_000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % wallpapers.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(360.dp),
            pageSpacing = 0.dp,
        ) { page ->
            val wallpaper = wallpapers[page]
            var favorite by remember(wallpaper.id) { mutableStateOf(favoriteStore.contains(wallpaper.id)) }
            Surface(
                onClick = { onWallpaperClick(wallpaper) },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box {
                    WallpaperImage(wallpaper, Modifier.fillMaxSize(), ContentScale.Crop)
                    IconButton(
                        onClick = { favorite = !favorite; favoriteStore.toggle(wallpaper.id) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).background(Color.Black.copy(alpha = 0.42f), CircleShape),
                    ) {
                        Icon(
                            if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (favorite) "Remove from favorites" else "Add to favorites",
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingPanel() {
    Surface(Modifier.fillMaxWidth().height(360.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}

@Composable
private fun LoadingGridItem() {
    Surface(Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) }
    }
}

@Composable
private fun ErrorPanel(message: String, retry: () -> Unit) {
    Surface(Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Couldn't load wallpapers", color = MaterialTheme.colorScheme.onSurface, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Button(onClick = retry) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyPanel(message: String) {
    Surface(Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

package com.darkstar.wallora.ui.explore

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.ui.components.WallpaperCard
import com.darkstar.wallora.ui.components.WallpaperImage

@Composable
fun ExploreScreen(
    repository: WallpaperRepository,
    favoriteStore: FavoriteStore,
    contentPadding: PaddingValues,
    onWallpaperClick: (Wallpaper) -> Unit,
) {
    var wallpapers by remember { mutableStateOf<List<Wallpaper>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        repository.getWallpapers()
            .onSuccess { wallpapers = it; loading = false }
            .onFailure { loading = false }
    }

    val categories = remember(wallpapers) {
        listOf("All") + wallpapers.map { it.category }.distinct().sorted()
    }
    val filtered = wallpapers
        .filter {
            (category == "All" || it.category == category) &&
                (query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true))
        }
        .sortedByDescending { it.addedAt ?: "" }

    val categoryPreviews = remember(wallpapers) {
        wallpapers.groupBy { it.category }
            .mapNotNull { (name, items) -> items.firstOrNull()?.let { name to it } }
            .sortedBy { it.first }
    }
    val featured = filtered.take(6)
    val trending = filtered.drop(6).take(6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Explore",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { searchOpen = !searchOpen },
                    modifier = Modifier.semantics {
                        contentDescription = if (searchOpen) "Close search" else "Search wallpapers"
                    },
                ) {
                    Icon(
                        if (searchOpen) Icons.Outlined.Close else Icons.Outlined.Search,
                        contentDescription = null,
                    )
                }
            }
            Text(
                "Find something that fits your screen.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (searchOpen) {
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    placeholder = { Text("Search wallpapers") },
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Browse by category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (categories.size > 1) {
                    Text(
                        "${categories.size - 1} categories",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    CategoryPreview(
                        name = "All",
                        wallpaper = filtered.firstOrNull() ?: wallpapers.firstOrNull(),
                        selected = category == "All",
                        onClick = { category = "All" },
                    )
                }
                items(categoryPreviews) { (name, wallpaper) ->
                    CategoryPreview(
                        name = name,
                        wallpaper = wallpaper,
                        selected = category == name,
                        onClick = { category = name },
                    )
                }
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filtered.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No wallpapers match your search.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                if (featured.isNotEmpty()) {
                    SectionHeader("Featured", featured.size)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(featured) { wallpaper ->
                            HorizontalWallpaperCard(
                                wallpaper = wallpaper,
                                favoriteStore = favoriteStore,
                                onClick = { onWallpaperClick(wallpaper) },
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                if (trending.isNotEmpty()) {
                    SectionHeader("Trending now", trending.size)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(trending) { wallpaper ->
                            HorizontalWallpaperCard(
                                wallpaper = wallpaper,
                                favoriteStore = favoriteStore,
                                onClick = { onWallpaperClick(wallpaper) },
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                SectionHeader("Latest wallpapers", filtered.size)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(filtered) { wallpaper ->
                        WallpaperCard(wallpaper, favoriteStore) { onWallpaperClick(wallpaper) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(
            count.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun CategoryPreview(
    name: String,
    wallpaper: Wallpaper?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(78.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(68.dp),
        ) {
            if (wallpaper != null) {
                Box {
                    WallpaperImage(
                        wallpaper = wallpaper,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    if (selected) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            name,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun HorizontalWallpaperCard(
    wallpaper: Wallpaper,
    favoriteStore: FavoriteStore,
    onClick: () -> Unit,
) {
    var favorite by remember(wallpaper.id) { mutableStateOf(favoriteStore.contains(wallpaper.id)) }

    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 180.dp, height = 230.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box {
            WallpaperImage(
                wallpaper = wallpaper,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.52f)),
            )
            Text(
                wallpaper.title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp, 10.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            IconButton(
                onClick = { favorite = !favorite; favoriteStore.toggle(wallpaper.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.42f), CircleShape),
            ) {
                Icon(
                    imageVector = if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (favorite) "Remove from favorites" else "Add to favorites",
                    tint = Color.White,
                )
            }
        }
    }
}

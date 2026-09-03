package com.darkstar.wallora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil3.compose.SubcomposeAsyncImage
import com.darkstar.wallora.R
import com.darkstar.wallora.data.FavoriteStore
import com.darkstar.wallora.data.ImageCacheManager
import com.darkstar.wallora.model.Wallpaper

val LocalImageCacheManager = staticCompositionLocalOf<ImageCacheManager> { error("ImageCacheManager is not provided") }

@Composable
fun WallpaperCard(wallpaper: Wallpaper, favoriteStore: FavoriteStore, onClick: () -> Unit) {
    var favorite by remember(wallpaper.id) { mutableStateOf(favoriteStore.contains(wallpaper.id)) }
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.card_height)), shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)), color = MaterialTheme.colorScheme.surfaceVariant) {
        Box {
            WallpaperImage(wallpaper, Modifier.fillMaxSize(), ContentScale.Crop)
            IconButton(onClick = { favorite = !favorite; favoriteStore.toggle(wallpaper.id) }, modifier = Modifier.align(Alignment.TopEnd).padding(dimensionResource(R.dimen.favorite_button_padding)).background(colorResource(R.color.favorite_scrim), CircleShape)) {
                Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = stringResource(if (favorite) R.string.remove_from_favorites else R.string.add_to_favorites), tint = colorResource(R.color.preview_text))
            }
        }
    }
}

@Composable
fun WallpaperImage(wallpaper: Wallpaper, modifier: Modifier, contentScale: ContentScale) {
    val cache = LocalImageCacheManager.current
    SubcomposeAsyncImage(
        model = cache.imageRequest(wallpaper.url), imageLoader = cache.imageLoader, contentDescription = wallpaper.title,
        modifier = modifier, contentScale = contentScale,
        loading = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(dimensionResource(R.dimen.progress_icon_size)), strokeWidth = dimensionResource(R.dimen.progress_stroke_width)) } },
        error = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text(stringResource(R.string.image_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) } },
    )
}

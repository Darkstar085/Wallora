package com.darkstar.wallora.ui.preview

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.darkstar.wallora.R
import com.darkstar.wallora.data.WallpaperApplier
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.model.WallpaperTarget
import com.darkstar.wallora.ui.components.WallpaperImage
import kotlinx.coroutines.launch

@Composable
fun WallpaperPreviewScreen(wallpaper: Wallpaper, isFavorite: Boolean, wallpaperTarget: WallpaperTarget, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var favorite by remember(wallpaper.id) { mutableStateOf(isFavorite) }
    var applying by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = colorResource(R.color.preview_background)) {
        Box(Modifier.fillMaxSize()) {
            WallpaperImage(wallpaper, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(
                Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(colorResource(R.color.transparent), colorResource(R.color.preview_overlay))))
                    .padding(dimensionResource(R.dimen.preview_content_padding)),
            ) {
                Column {
                    Text(wallpaper.title, color = colorResource(R.color.preview_text), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(R.string.wallpaper_metadata, wallpaper.category, wallpaper.width, wallpaper.height), color = colorResource(R.color.preview_secondary_text))
                    Spacer(Modifier.size(dimensionResource(R.dimen.preview_content_spacing)))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.preview_button_spacing)), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            if (applying) return@Button
                            applying = true
                            resultMessage = null
                            scope.launch {
                                WallpaperApplier(context).apply(wallpaper.url, wallpaperTarget)
                                    .onSuccess { resultMessage = context.getString(R.string.wallpaper_applied, wallpaperTarget.label(context)) }
                                    .onFailure { resultMessage = it.message ?: context.getString(R.string.couldnt_apply_wallpaper) }
                                applying = false
                            }
                        }, modifier = Modifier.weight(1f)) {
                            if (applying) CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.preview_button_icon_size)), strokeWidth = dimensionResource(R.dimen.progress_stroke_width)) else Text(stringResource(R.string.set_wallpaper))
                        }
                        IconButton(onClick = { favorite = !favorite; onToggleFavorite() }, modifier = Modifier.size(dimensionResource(R.dimen.preview_favorite_size)).background(colorResource(R.color.favorite_button_light_scrim), CircleShape)) {
                            Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = stringResource(R.string.favorite), tint = colorResource(R.color.preview_text))
                        }
                    }
                    resultMessage?.let {
                        Spacer(Modifier.size(dimensionResource(R.dimen.tiny_spacing)))
                        Text(it, color = colorResource(R.color.preview_text), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(dimensionResource(R.dimen.preview_back_padding)).background(colorResource(R.color.favorite_scrim), CircleShape)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = colorResource(R.color.preview_text))
            }
        }
    }
}

private fun WallpaperTarget.label(context: Context): String = when (this) {
    WallpaperTarget.HOME -> context.getString(R.string.home_screen)
    WallpaperTarget.LOCK -> context.getString(R.string.lock_screen)
    WallpaperTarget.BOTH -> context.getString(R.string.home_and_lock_screen)
}

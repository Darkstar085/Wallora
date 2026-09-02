package com.darkstar.wallora.ui.preview

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkstar.wallora.data.WallpaperApplier
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.model.WallpaperTarget
import com.darkstar.wallora.ui.components.WallpaperImage
import kotlinx.coroutines.launch

@Composable
fun WallpaperPreviewScreen(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    wallpaperTarget: WallpaperTarget,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var favorite by remember(wallpaper.id) { mutableStateOf(isFavorite) }
    var applying by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(Modifier.fillMaxSize()) {
            WallpaperImage(wallpaper, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(
                Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF20D0B10))))
                    .padding(20.dp),
            ) {
                Column {
                    Text(wallpaper.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("${wallpaper.category} · ${wallpaper.width} × ${wallpaper.height}", color = Color(0xFFD0C8D4))
                    Spacer(Modifier.size(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            if (applying) return@Button
                            applying = true
                            resultMessage = null
                            scope.launch {
                                WallpaperApplier(context).apply(wallpaper.url, wallpaperTarget)
                                    .onSuccess { resultMessage = "Wallpaper applied to ${wallpaperTarget.label.lowercase()}" }
                                    .onFailure { resultMessage = it.message ?: "Couldn't apply wallpaper" }
                                applying = false
                            }
                        }, modifier = Modifier.weight(1f)) {
                            if (applying) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Set wallpaper")
                        }
                        IconButton(onClick = { favorite = !favorite; onToggleFavorite() }, modifier = Modifier.size(50.dp).background(Color.White.copy(alpha = 0.16f), CircleShape)) {
                            Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorite", tint = Color.White)
                        }
                    }
                    resultMessage?.let {
                        Spacer(Modifier.size(8.dp))
                        Text(it, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(14.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    }
}

package com.darkstar.wallora.ui.preview

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.core.content.ContextCompat
import com.darkstar.wallora.R
import com.darkstar.wallora.data.NotificationHelper
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.WallpaperApplier
import com.darkstar.wallora.data.WallpaperDownloader
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.Wallpaper
import com.darkstar.wallora.model.WallpaperTarget
import com.darkstar.wallora.ui.components.WallpaperImage
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun WallpaperPreviewScreen(wallpaper: Wallpaper, isFavorite: Boolean, preferences: PreferencesStore, repository: WallpaperRepository, onBack: () -> Unit, onToggleFavorite: (String) -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var wallpapers by remember { mutableStateOf(listOf(wallpaper)) }
    var favorite by remember(wallpaper.id) { mutableStateOf(isFavorite) }
    var applying by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { repository.getWallpapers().onSuccess { if (it.isNotEmpty()) wallpapers = it } }
    val pagerState = rememberPagerState(pageCount = { wallpapers.size })
    LaunchedEffect(wallpapers) {
        val target = wallpapers.indexOfFirst { it.id == wallpaper.id }.coerceAtLeast(0)
        if (target != pagerState.currentPage) pagerState.scrollToPage(target)
    }
    val currentWallpaper = wallpapers.getOrElse(pagerState.currentPage) { wallpaper }

    fun performDownload() {
        if (downloading) return
        downloading = true
        resultMessage = null
        scope.launch {
            WallpaperDownloader(context).download(currentWallpaper, preferences.downloadLocationUri)
                .onSuccess {
                    resultMessage = "Saved ${currentWallpaper.filename}"
                    NotificationHelper.showDownloadComplete(context, currentWallpaper.filename)
                }
                .onFailure { resultMessage = it.message ?: "Couldn't download wallpaper" }
            downloading = false
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { performDownload() }

    fun downloadWallpaper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            performDownload()
        }
    }

    BackHandler { onBack() }

    fun applyWallpaper(target: WallpaperTarget) {
        if (applying) return
        applying = true
        resultMessage = null
        showTargetDialog = false
        scope.launch {
            WallpaperApplier(context).apply(currentWallpaper.url, target)
                .onSuccess { resultMessage = context.getString(R.string.wallpaper_applied, target.label(context)) }
                .onFailure { resultMessage = it.message ?: context.getString(R.string.couldnt_apply_wallpaper) }
            applying = false
        }
    }

    Surface(Modifier.fillMaxSize(), color = colorResource(R.color.preview_background)) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page -> WallpaperImage(wallpapers[page], Modifier.fillMaxSize(), ContentScale.Crop) }
            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(colorResource(R.color.transparent), colorResource(R.color.preview_overlay)))).padding(dimensionResource(R.dimen.preview_content_padding))) {
                Column {
                    Text(currentWallpaper.title, color = colorResource(R.color.preview_text), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.size(dimensionResource(R.dimen.preview_content_spacing)))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.preview_button_spacing)), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { showTargetDialog = true }, enabled = !applying && !downloading, modifier = Modifier.weight(1f)) { if (applying) CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.preview_button_icon_size)), strokeWidth = dimensionResource(R.dimen.progress_stroke_width)) else Text(stringResource(R.string.set_wallpaper)) }
                        IconButton(onClick = ::downloadWallpaper, enabled = !downloading, modifier = Modifier.size(dimensionResource(R.dimen.preview_favorite_size)).background(colorResource(R.color.favorite_button_light_scrim), CircleShape)) { if (downloading) CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.preview_button_icon_size)), strokeWidth = dimensionResource(R.dimen.progress_stroke_width)) else Icon(Icons.Outlined.Download, contentDescription = stringResource(R.string.download), tint = colorResource(R.color.preview_text)) }
                        IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.size(dimensionResource(R.dimen.preview_favorite_size)).background(colorResource(R.color.favorite_button_light_scrim), CircleShape)) { Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.info), tint = colorResource(R.color.preview_text)) }
                        IconButton(onClick = { favorite = !favorite; onToggleFavorite(currentWallpaper.id) }, modifier = Modifier.size(dimensionResource(R.dimen.preview_favorite_size)).background(colorResource(R.color.favorite_button_light_scrim), CircleShape)) { Icon(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = stringResource(R.string.favorite), tint = colorResource(R.color.preview_text)) }
                    }
                    resultMessage?.let { Spacer(Modifier.size(dimensionResource(R.dimen.tiny_spacing))); Text(it, color = colorResource(R.color.preview_text), style = MaterialTheme.typography.bodySmall) }
                }
            }
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(dimensionResource(R.dimen.preview_back_padding)).background(colorResource(R.color.favorite_scrim), CircleShape)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back), tint = colorResource(R.color.preview_text)) }
        }
    }

    if (showTargetDialog) AlertDialog(onDismissRequest = { showTargetDialog = false }, title = { Text(stringResource(R.string.set_wallpaper)) }, text = { Column { WallpaperTarget.entries.forEach { target -> Row(Modifier.fillMaxWidth().clickable { applyWallpaper(target) }.padding(vertical = dimensionResource(R.dimen.settings_dialog_row_padding)), verticalAlignment = Alignment.CenterVertically) { RadioButton(false, { applyWallpaper(target) }); Text(target.label(context), Modifier.padding(start = dimensionResource(R.dimen.settings_dialog_label_padding))) } } } }, confirmButton = { TextButton(onClick = { showTargetDialog = false }) { Text(stringResource(R.string.cancel)) } })
    if (showInfoDialog) AlertDialog(onDismissRequest = { showInfoDialog = false }, title = { Text(currentWallpaper.title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tiny_spacing))) { Text("Filename: ${currentWallpaper.filename}"); Text("Dimensions: ${currentWallpaper.width} × ${currentWallpaper.height}"); Text("File size: ${formatBytes(currentWallpaper.fileSizeBytes)}"); Text("Format: ${currentWallpaper.format.uppercase(Locale.US)}") } }, confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text(stringResource(R.string.close)) } })
}

private fun formatBytes(bytes: Long): String { if (bytes <= 0) return "Unknown"; if (bytes < 1024) return "$bytes B"; val units = arrayOf("KB", "MB", "GB"); var value = bytes.toDouble(); var index = -1; while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }; return String.format(Locale.US, "%.1f %s", value, units[index]) }
private fun WallpaperTarget.label(context: Context): String = when (this) { WallpaperTarget.HOME -> context.getString(R.string.home_screen); WallpaperTarget.LOCK -> context.getString(R.string.lock_screen); WallpaperTarget.BOTH -> context.getString(R.string.home_and_lock_screen) }

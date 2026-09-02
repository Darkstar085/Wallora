package com.darkstar.wallora.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkstar.wallora.BuildConfig
import com.darkstar.wallora.data.ImageCacheManager
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.WallpaperRepository
import com.darkstar.wallora.model.ThemeMode
import com.darkstar.wallora.model.WallpaperTarget

@Composable
fun SettingsScreen(contentPadding: PaddingValues, preferences: PreferencesStore, imageCache: ImageCacheManager) {
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var cacheSize by remember { mutableStateOf(imageCache.formattedSize()) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item { SettingsHeader() }
        item { AppearanceSection(preferences) { dialog = SettingsDialog.THEME } }
        item { WallpaperSection(preferences.wallpaperTarget) { dialog = SettingsDialog.WALLPAPER_TARGET } }
        item { CacheSection(cacheSize) { showClearCacheDialog = true } }
        item { AboutSection() }
        item { VersionSection() }
    }

    when (dialog) {
        SettingsDialog.THEME -> ThemeDialog(
            selected = preferences.themeMode,
            onSelected = { preferences.updateThemeMode(it); dialog = null },
            onDismiss = { dialog = null },
        )
        SettingsDialog.WALLPAPER_TARGET -> WallpaperTargetDialog(
            selected = preferences.wallpaperTarget,
            onSelected = { preferences.updateWallpaperTarget(it); dialog = null },
            onDismiss = { dialog = null },
        )
        null -> Unit
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear cache?") },
            text = { Text("Downloaded wallpapers and cached wallpaper data will be removed.") },
            dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = {
                    imageCache.clear()
                    cacheSize = imageCache.formattedSize()
                    showClearCacheDialog = false
                }) { Text("Clear") }
            },
        )
    }
}

private enum class SettingsDialog { THEME, WALLPAPER_TARGET }

@Composable
private fun SettingsHeader() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Simple controls and information.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppearanceSection(preferences: PreferencesStore, onThemeClick: () -> Unit) {
    SettingsSection("Appearance") {
        SettingsPreference(
            icon = { Icon(Icons.Outlined.DarkMode, null) },
            title = "Theme",
            subtitle = preferences.themeMode.label,
            onClick = onThemeClick,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitch(
                icon = { Icon(Icons.Outlined.Palette, null) },
                title = "Dynamic colors",
                subtitle = "Use colors from your device",
                checked = preferences.dynamicColors,
                onCheckedChange = preferences::updateDynamicColors,
            )
        }
    }
}

@Composable
private fun WallpaperSection(target: WallpaperTarget, onTargetClick: () -> Unit) {
    SettingsSection("Wallpaper") {
        SettingsPreference(
            icon = { Icon(Icons.Outlined.Wallpaper, null) },
            title = "Apply wallpaper",
            subtitle = target.label,
            onClick = onTargetClick,
        )
    }
}

@Composable
private fun CacheSection(size: String, onClearClick: () -> Unit) {
    SettingsSection("Cache") {
        ListItem(
            headlineContent = { Text("Wallpaper cache") },
            supportingContent = { Text(size) },
            leadingContent = { Icon(Icons.Outlined.Cached, null) },
            trailingContent = { TextButton(onClick = onClearClick) { Text("Clear") } },
        )
        SettingsDivider()
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    SettingsSection("About") {
        ListItem(
            headlineContent = { Text("Wallora") },
            supportingContent = { Text("A simple open-source wallpaper app built with Kotlin and Jetpack Compose.") },
            leadingContent = { Icon(Icons.Outlined.Info, null) },
        )
        SettingsPreference(
            icon = null,
            title = "Wallpaper catalog",
            subtitle = "Darkstar085/Wallpapers",
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WallpaperRepository.API_URL))) },
        )
    }
}

@Composable
private fun VersionSection() {
    SettingsSection("Version") {
        ListItem(
            headlineContent = { Text("Wallora") },
            supportingContent = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Column(content = content)
    }
}

@Composable
private fun SettingsPreference(
    icon: (@Composable () -> Unit)?,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        trailingContent = {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
    SettingsDivider()
}

@Composable
private fun SettingsSwitch(
    icon: (@Composable () -> Unit)?,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
    SettingsDivider()
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    )
}

@Composable
private fun ThemeDialog(selected: ThemeMode, onSelected: (ThemeMode) -> Unit, onDismiss: () -> Unit) =
    SettingsSelectionDialog("Theme", ThemeMode.entries, selected, { it.label }, onSelected, onDismiss)

@Composable
private fun WallpaperTargetDialog(selected: WallpaperTarget, onSelected: (WallpaperTarget) -> Unit, onDismiss: () -> Unit) =
    SettingsSelectionDialog("Apply wallpaper", WallpaperTarget.entries, selected, { it.label }, onSelected, onDismiss)

@Composable
private fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == option, onClick = { onSelected(option) })
                        Text(label(option), Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.darkstar.wallora.BuildConfig
import com.darkstar.wallora.R
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
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + dimensionResource(R.dimen.settings_header_vertical_padding), bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.screen_vertical_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_list_spacing)),
    ) {
        item { SettingsHeader() }
        item { AppearanceSection(preferences) { dialog = SettingsDialog.THEME } }
        item { WallpaperSection(preferences.wallpaperTarget) { dialog = SettingsDialog.WALLPAPER_TARGET } }
        item { CacheSection(cacheSize) { showClearCacheDialog = true } }
        item { AboutSection() }
        item { VersionSection() }
    }

    when (dialog) {
        SettingsDialog.THEME -> ThemeDialog(preferences.themeMode, { preferences.updateThemeMode(it); dialog = null }, { dialog = null })
        SettingsDialog.WALLPAPER_TARGET -> WallpaperTargetDialog(preferences.wallpaperTarget, { preferences.updateWallpaperTarget(it); dialog = null }, { dialog = null })
        null -> Unit
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.clear_cache_title)) },
            text = { Text(stringResource(R.string.clear_cache_message)) },
            dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text(stringResource(R.string.cancel)) } },
            confirmButton = { TextButton(onClick = { imageCache.clear(); cacheSize = imageCache.formattedSize(); showClearCacheDialog = false }) { Text(stringResource(R.string.clear)) } },
        )
    }
}

private enum class SettingsDialog { THEME, WALLPAPER_TARGET }

@Composable
private fun SettingsHeader() {
    Column(Modifier.padding(horizontal = dimensionResource(R.dimen.settings_header_horizontal_padding), vertical = dimensionResource(R.dimen.settings_header_vertical_padding)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_header_spacing))) {
        Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.settings_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppearanceSection(preferences: PreferencesStore, onThemeClick: () -> Unit) {
    SettingsSection(stringResource(R.string.appearance)) {
        SettingsPreference({ Icon(Icons.Outlined.DarkMode, null) }, stringResource(R.string.theme), preferences.themeLabel(), onThemeClick)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) SettingsSwitch({ Icon(Icons.Outlined.Palette, null) }, stringResource(R.string.dynamic_colors), stringResource(R.string.dynamic_colors_subtitle), preferences.dynamicColors, preferences::updateDynamicColors)
    }
}

@Composable
private fun WallpaperSection(target: WallpaperTarget, onTargetClick: () -> Unit) {
    SettingsSection(stringResource(R.string.wallpaper)) {
        SettingsPreference({ Icon(Icons.Outlined.Wallpaper, null) }, stringResource(R.string.apply_wallpaper), target.label(), onTargetClick)
    }
}

@Composable
private fun CacheSection(size: String, onClearClick: () -> Unit) {
    SettingsSection(stringResource(R.string.cache)) {
        ListItem(headlineContent = { Text(stringResource(R.string.wallpaper_cache)) }, supportingContent = { Text(size) }, leadingContent = { Icon(Icons.Outlined.Cached, null) }, trailingContent = { TextButton(onClick = onClearClick) { Text(stringResource(R.string.clear)) } })
        SettingsDivider()
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    SettingsSection(stringResource(R.string.about)) {
        ListItem(headlineContent = { Text(stringResource(R.string.app_name)) }, supportingContent = { Text(stringResource(R.string.about_description)) }, leadingContent = { Icon(Icons.Outlined.Info, null) })
        SettingsPreference(null, stringResource(R.string.wallpaper_catalog), stringResource(R.string.wallpaper_catalog_value), { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WallpaperRepository.API_URL))) })
    }
}

@Composable
private fun VersionSection() {
    SettingsSection(stringResource(R.string.version)) {
        ListItem(headlineContent = { Text(stringResource(R.string.app_name)) }, supportingContent = { Text(stringResource(R.string.version_value, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)) })
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, Modifier.padding(start = dimensionResource(R.dimen.settings_header_horizontal_padding), top = dimensionResource(R.dimen.settings_section_top_padding), bottom = dimensionResource(R.dimen.settings_section_bottom_padding)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Column(content = content)
    }
}

@Composable
private fun SettingsPreference(icon: (@Composable () -> Unit)?, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        headlineContent = { Text(title) }, supportingContent = { Text(subtitle) }, leadingContent = icon,
        trailingContent = { Text(stringResource(R.string.preference_arrow), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
    )
    SettingsDivider()
}

@Composable
private fun SettingsSwitch(icon: (@Composable () -> Unit)?, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(modifier = Modifier.fillMaxWidth(), headlineContent = { Text(title) }, supportingContent = { Text(subtitle) }, leadingContent = icon, trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) })
    SettingsDivider()
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(horizontal = dimensionResource(R.dimen.settings_divider_horizontal_padding)), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
}

@Composable
private fun ThemeDialog(selected: ThemeMode, onSelected: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val options = ThemeMode.entries.map { it to it.label() }
    SettingsSelectionDialog(stringResource(R.string.theme), options, selected, onSelected, onDismiss)
}

@Composable
private fun WallpaperTargetDialog(selected: WallpaperTarget, onSelected: (WallpaperTarget) -> Unit, onDismiss: () -> Unit) {
    val options = WallpaperTarget.entries.map { it to it.label() }
    SettingsSelectionDialog(stringResource(R.string.apply_wallpaper), options, selected, onSelected, onDismiss)
}

@Composable
private fun <T> SettingsSelectionDialog(title: String, options: List<Pair<T, String>>, selected: T, onSelected: (T) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (option, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onSelected(option) }.padding(vertical = dimensionResource(R.dimen.settings_dialog_row_padding)), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected == option, { onSelected(option) })
                        Text(label, Modifier.padding(start = dimensionResource(R.dimen.settings_dialog_label_padding)))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun PreferencesStore.themeLabel(): String = when (themeMode) {
    ThemeMode.SYSTEM -> stringResource(R.string.system_default)
    ThemeMode.LIGHT -> stringResource(R.string.light)
    ThemeMode.DARK -> stringResource(R.string.dark)
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.system_default)
    ThemeMode.LIGHT -> stringResource(R.string.light)
    ThemeMode.DARK -> stringResource(R.string.dark)
}

@Composable
private fun WallpaperTarget.label(): String = when (this) {
    WallpaperTarget.HOME -> stringResource(R.string.home_screen)
    WallpaperTarget.LOCK -> stringResource(R.string.lock_screen)
    WallpaperTarget.BOTH -> stringResource(R.string.home_and_lock_screen)
}

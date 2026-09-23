package com.darkstar.wallora.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.darkstar.wallora.BuildConfig
import com.darkstar.wallora.R
import com.darkstar.wallora.data.AppUpdate
import com.darkstar.wallora.data.ImageCacheManager
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.UpdateManager
import com.darkstar.wallora.data.WallpaperRepository
import androidx.core.graphics.drawable.toBitmap
import com.darkstar.wallora.MainActivity
import com.darkstar.wallora.model.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(contentPadding: PaddingValues, preferences: PreferencesStore, imageCache: ImageCacheManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var cacheSize by remember { mutableStateOf(imageCache.formattedSize()) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var downloadingUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var downloadedUpdate by remember { mutableStateOf<AppUpdate?>(null) }
    val appIcon = remember { context.packageManager.getApplicationIcon(context.applicationInfo).toBitmap().asImageBitmap() }
    var showNoUpdateDialog by remember { mutableStateOf(false) }

    fun checkForUpdate(showResult: Boolean = false) {
        if (checkingUpdate) return
        checkingUpdate = true
        updateMessage = null
        scope.launch {
            UpdateManager.checkForUpdate()
                .onSuccess { latest ->
                    update = latest
                    if (latest == null) { updateMessage = context.getString(R.string.up_to_date); if (showResult) showNoUpdateDialog = true }
                }
                .onFailure { error ->
                    updateMessage = error.message ?: context.getString(R.string.update_check_failed)
                }
            checkingUpdate = false
        }
    }

    LaunchedEffect(Unit) {
        val pending = withContext(kotlinx.coroutines.Dispatchers.IO) { UpdateManager.getPendingUpdate(context) }
        if (pending != null) {
            update = pending
            downloadedUpdate = withContext(kotlinx.coroutines.Dispatchers.IO) { UpdateManager.getDownloadedUpdate(context) }
        } else checkForUpdate()
    }

    LaunchedEffect(update?.tag, downloadingUpdate) {
        val current = update ?: return@LaunchedEffect
        if (!downloadingUpdate || downloadedUpdate?.tag == current.tag) return@LaunchedEffect
        while (true) {
            delay(1500)
            val downloaded = withContext(kotlinx.coroutines.Dispatchers.IO) { UpdateManager.getDownloadedUpdate(context) }
            if (downloaded != null) { downloadedUpdate = downloaded; downloadingUpdate = false; break }
        }
    }


    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val persisted = runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }.isSuccess
        if (persisted) preferences.updateDownloadLocationUri(uri.toString())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + dimensionResource(R.dimen.settings_header_vertical_padding),
            bottom = contentPadding.calculateBottomPadding() + dimensionResource(R.dimen.screen_vertical_padding),
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_list_spacing)),
    ) {
        item { SettingsHeader() }
        item { AppearanceSection(preferences) { dialog = SettingsDialog.THEME } }
        item {
            SettingsSection(stringResource(R.string.downloads)) {
                SettingsPreference(
                    { Icon(Icons.Outlined.Download, null) },
                    stringResource(R.string.download_location),
                    preferences.downloadLocationUri ?: stringResource(R.string.default_download_location),
                    { folderPicker.launch(null) },
                )
            }
        }
        item { CacheSection(cacheSize) { showClearCacheDialog = true } }
        item { AboutSection() }
        item {
            UpdateSection(
                update = update,
                checking = checkingUpdate,
                downloading = downloadingUpdate,
                message = updateMessage,
                onCheck = { checkForUpdate(showResult = true) },
                onOpen = { if (update != null) dialog = SettingsDialog.UPDATE },
            )
        }
        item { VersionSection() }
    }

    when (dialog) {
        SettingsDialog.THEME -> ThemeDialog(
            preferences.themeMode,
            { preferences.updateThemeMode(it); dialog = null },
            { dialog = null },
        )
        SettingsDialog.UPDATE -> update?.let { latest ->
            UpdateDialog(
                update = latest,
                appIcon = appIcon,
                isDownloaded = downloadedUpdate?.tag == latest.tag,
                isDownloading = downloadingUpdate && downloadedUpdate?.tag != latest.tag,
                onDownload = {
                    if (downloadingUpdate) return@UpdateDialog
                    downloadingUpdate = true
                    UpdateManager.enqueueDownload(context, latest)
                },
                onInstall = { installDownloadedUpdate(context) },
                onDismiss = { dialog = null; downloadedUpdate = null; downloadingUpdate = false },
            )
        }
        null -> Unit
    }

    if (showNoUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showNoUpdateDialog = false },
            title = { Text(stringResource(R.string.no_update_available_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.no_update_available_message, BuildConfig.VERSION_NAME), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { Button(onClick = { showNoUpdateDialog = false }) { Text(stringResource(R.string.ok)) } },
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.clear_cache_title)) },
            text = { Text(stringResource(R.string.clear_cache_message)) },
            dismissButton = { TextButton(onClick = { showClearCacheDialog = false }) { Text(stringResource(R.string.cancel)) } },
            confirmButton = {
                TextButton(onClick = {
                    imageCache.clear()
                    cacheSize = imageCache.formattedSize()
                    showClearCacheDialog = false
                }) { Text(stringResource(R.string.clear)) }
            },
        )
    }
}

private enum class SettingsDialog { THEME, UPDATE }

@Composable
private fun SettingsHeader() {
    Column(
        Modifier.padding(
            horizontal = dimensionResource(R.dimen.settings_header_horizontal_padding),
            vertical = dimensionResource(R.dimen.settings_header_vertical_padding),
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_header_spacing)),
    ) {
        Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.settings_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppearanceSection(preferences: PreferencesStore, onThemeClick: () -> Unit) {
    SettingsSection(stringResource(R.string.appearance)) {
        SettingsPreference({ Icon(Icons.Outlined.DarkMode, null) }, stringResource(R.string.theme), preferences.themeLabel(), onThemeClick)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitch(
                { Icon(Icons.Outlined.Palette, null) },
                stringResource(R.string.dynamic_colors),
                stringResource(R.string.dynamic_colors_subtitle),
                preferences.dynamicColors,
                preferences::updateDynamicColors,
            )
        }
    }
}

@Composable
private fun CacheSection(size: String, onClearClick: () -> Unit) {
    SettingsSection(stringResource(R.string.cache)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.wallpaper_cache)) },
            supportingContent = { Text(size) },
            leadingContent = { Icon(Icons.Outlined.Cached, null) },
            trailingContent = { TextButton(onClick = onClearClick) { Text(stringResource(R.string.clear)) } },
        )
        SettingsDivider()
    }
}

@Composable
private fun AboutSection() {
    val context = LocalContext.current
    SettingsSection(stringResource(R.string.about)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.app_name)) },
            supportingContent = { Text(stringResource(R.string.about_description)) },
            leadingContent = { Icon(Icons.Outlined.Info, null) },
        )
        SettingsPreference(
            null,
            stringResource(R.string.wallpaper_catalog),
            stringResource(R.string.wallpaper_catalog_value),
            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WallpaperRepository.API_URL))) },
        )
    }
}

@Composable
private fun UpdateSection(
    update: AppUpdate?,
    checking: Boolean,
    downloading: Boolean,
    message: String?,
    onCheck: () -> Unit,
    onOpen: () -> Unit,
) {
    SettingsSection(stringResource(R.string.updates)) {
        ListItem(
            modifier = Modifier.fillMaxWidth().clickable(onClick = if (update != null) onOpen else onCheck),
            headlineContent = {
                Text(
                    if (update != null) stringResource(R.string.update_available, update.version)
                    else stringResource(R.string.check_for_updates),
                )
            },
            supportingContent = {
                Text(
                    when {
                        downloading -> stringResource(R.string.downloading_update)
                        checking -> stringResource(R.string.checking_for_updates)
                        message != null -> message
                        else -> stringResource(R.string.update_subtitle)
                    },
                )
            },
            leadingContent = {
                if (checking || downloading) {
                    CircularProgressIndicator()
                } else {
                    Icon(Icons.Outlined.Download, null)
                }
            },
            trailingContent = {
                if (!checking && !downloading) {
                    TextButton(onClick = if (update != null) onOpen else onCheck) {
                        Text(stringResource(if (update != null) R.string.view_update else R.string.check))
                    }
                }
            },
        )
        SettingsDivider()
    }
}

@Composable
private fun UpdateDialog(
    update: AppUpdate,
    appIcon: androidx.compose.ui.graphics.ImageBitmap,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { androidx.compose.foundation.Image(bitmap = appIcon, contentDescription = stringResource(R.string.app_name), modifier = Modifier.size(58.dp)) },
        title = { Text(stringResource(if (isDownloaded) R.string.update_downloaded else R.string.update_available, update.version), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_dialog_row_padding))) {
                Text(stringResource(R.string.update_version_available, update.version), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.download_size, formatUpdateSize(update.size)))
                    update.releaseDate?.let { Text(stringResource(R.string.released, it)) }
                }
                if (update.notes.isNotEmpty()) {
                    Column {
                        Text(stringResource(R.string.whats_new), fontWeight = FontWeight.SemiBold)
                        Column(Modifier.fillMaxWidth().heightIn(max = 128.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            update.notes.forEach { note -> Text("• $note", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                TextButton(onClick = onDismiss, enabled = !isDownloading) { Text(stringResource(R.string.later)) }
                Button(onClick = if (isDownloaded) onInstall else onDownload, enabled = !isDownloading) {
                    Text(stringResource(when { isDownloaded -> R.string.install_update; isDownloading -> R.string.downloading_update; else -> R.string.download_update }))
                }
            }
        },
    )
}

private fun formatUpdateSize(bytes: Long): String = when {
    bytes <= 0L -> "—"
    bytes < 1024L * 1024L -> "%.0f KB".format(bytes / 1024f)
    else -> "%.1f MB".format(bytes / (1024f * 1024f))
}

private fun installDownloadedUpdate(context: android.content.Context) {
    context.startActivity(Intent(context, MainActivity::class.java).apply { action = UpdateManager.ACTION_INSTALL_UPDATE; flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP })
}

@Composable
private fun VersionSection() {
    SettingsSection(stringResource(R.string.version)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.app_name)) },
            supportingContent = { Text(stringResource(R.string.version_value, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)) },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            Modifier.padding(
                start = dimensionResource(R.dimen.settings_header_horizontal_padding),
                top = dimensionResource(R.dimen.settings_section_top_padding),
                bottom = dimensionResource(R.dimen.settings_section_bottom_padding),
            ),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Column(content = content)
    }
}

@Composable
private fun SettingsPreference(icon: (@Composable () -> Unit)?, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        trailingContent = {
            Text(
                stringResource(R.string.preference_arrow),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
    SettingsDivider()
}

@Composable
private fun SettingsSwitch(icon: (@Composable () -> Unit)?, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
    SettingsDivider()
}

@Composable private fun SettingsDivider() {
    HorizontalDivider(
        Modifier.padding(horizontal = dimensionResource(R.dimen.settings_divider_horizontal_padding)),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    )
}

@Composable private fun ThemeDialog(selected: ThemeMode, onSelected: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    SettingsSelectionDialog(stringResource(R.string.theme), ThemeMode.entries.map { it to it.label() }, selected, onSelected, onDismiss)
}

@Composable
private fun <T> SettingsSelectionDialog(title: String, options: List<Pair<T, String>>, selected: T, onSelected: (T) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (option, label) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelected(option) }.padding(vertical = dimensionResource(R.dimen.settings_dialog_row_padding)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == option, { onSelected(option) })
                        Text(label, Modifier.padding(start = dimensionResource(R.dimen.settings_dialog_label_padding)))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable private fun PreferencesStore.themeLabel(): String = when (themeMode) {
    ThemeMode.SYSTEM -> stringResource(R.string.system_default)
    ThemeMode.LIGHT -> stringResource(R.string.light)
    ThemeMode.DARK -> stringResource(R.string.dark)
}

@Composable private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.system_default)
    ThemeMode.LIGHT -> stringResource(R.string.light)
    ThemeMode.DARK -> stringResource(R.string.dark)
}

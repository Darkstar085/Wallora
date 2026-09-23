package com.darkstar.wallora.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.darkstar.wallora.BuildConfig
import com.darkstar.wallora.MainActivity
import com.darkstar.wallora.R
import com.darkstar.wallora.data.AppUpdate
import com.darkstar.wallora.data.ImageCacheManager
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.UpdateManager
import com.darkstar.wallora.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    preferences: PreferencesStore,
    imageCache: ImageCacheManager,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var cacheSize by remember { mutableStateOf(imageCache.formattedSize()) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var showNoUpdateDialog by remember { mutableStateOf(false) }

    val appIcon = remember {
        context.packageManager
            .getApplicationIcon(context.applicationInfo)
            .toBitmap()
            .asImageBitmap()
    }

    fun checkForUpdate() {
        if (checkingUpdate) return
        checkingUpdate = true
        updateMessage = null
        showNoUpdateDialog = false
        scope.launch {
            UpdateManager.checkForUpdate()
                .onSuccess { latest ->
                    update = latest
                    updateMessage = null
                    showNoUpdateDialog = latest == null
                }
                .onFailure { error ->
                    update = null
                    updateMessage = error.message
                        ?: context.getString(R.string.update_check_failed)
                    showNoUpdateDialog = true
                }
            checkingUpdate = false
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val persisted = runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }.isSuccess
        if (persisted) {
            preferences.updateDownloadLocationUri(uri.toString())
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() +
                dimensionResource(R.dimen.settings_header_vertical_padding),
            bottom = contentPadding.calculateBottomPadding() +
                dimensionResource(R.dimen.screen_vertical_padding),
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { SettingsHeader() }
        item {
            AppearanceSection(
                preferences = preferences,
                onThemeClick = { dialog = SettingsDialog.THEME },
            )
        }
        item {
            StorageSection(
                downloadLocation = preferences.downloadLocationUri
                    ?: stringResource(R.string.default_download_location),
                cacheSize = cacheSize,
                onDownloadLocationClick = { folderPicker.launch(null) },
                onClearCacheClick = { showClearCacheDialog = true },
            )
        }
        item {
            SystemSection(
                onNotificationsClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        },
                    )
                },
                onUnknownAppsClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${context.packageName}"),
                            ),
                        )
                    }
                },
            )
        }
        item {
            AboutSection(
                onAboutClick = { dialog = SettingsDialog.ABOUT },
                onOpenSourceClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/Darkstar085/Wallora"),
                        ),
                    )
                },
            )
        }
    }

    when (dialog) {
        SettingsDialog.THEME -> ThemeDialog(
            selected = preferences.themeMode,
            onSelected = {
                preferences.updateThemeMode(it)
                dialog = null
            },
            onDismiss = { dialog = null },
        )

        SettingsDialog.ABOUT -> AboutDialog(
            appIcon = appIcon,
            onCheckForUpdate = { checkForUpdate() },
            onDismiss = { dialog = null },
        )

        null -> Unit
    }

    if (showNoUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showNoUpdateDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            },
            title = {
                Text(
                    if (updateMessage == null) {
                        stringResource(R.string.no_update_available_title)
                    } else {
                        stringResource(R.string.update_check_failed)
                    },
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    updateMessage
                        ?: stringResource(
                            R.string.no_update_available_message,
                            BuildConfig.VERSION_NAME,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { showNoUpdateDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp,
        )
    }

    update?.let { latest ->
        if (!checkingUpdate) {
            UpdateAvailableDialog(
                update = latest,
                onDownload = {
                    UpdateManager.savePendingUpdate(context, latest)
                    UpdateManager.enqueueDownload(context, latest)
                    update = null
                },
                onDismiss = { update = null },
            )
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.clear_cache_title)) },
            text = { Text(stringResource(R.string.clear_cache_message)) },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        imageCache.clear()
                        cacheSize = imageCache.formattedSize()
                        showClearCacheDialog = false
                    },
                ) {
                    Text(stringResource(R.string.clear))
                }
            },
        )
    }
}

private enum class SettingsDialog {
    THEME,
    ABOUT,
}

@Composable
private fun SettingsHeader() {
    Column(
        Modifier.padding(
            horizontal = dimensionResource(R.dimen.settings_header_horizontal_padding),
            vertical = 6.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.settings_header_spacing)),
    ) {
        Text(
            stringResource(R.string.tab_settings),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(R.string.settings_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppearanceSection(
    preferences: PreferencesStore,
    onThemeClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.appearance),
        subtitle = stringResource(R.string.appearance_subtitle),
    ) {
        SettingsPreference(
            icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
            title = stringResource(R.string.theme),
            subtitle = preferences.themeLabel(),
            onClick = onThemeClick,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitch(
                icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                title = stringResource(R.string.dynamic_colors),
                subtitle = stringResource(R.string.dynamic_colors_subtitle),
                checked = preferences.dynamicColors,
                onCheckedChange = preferences::updateDynamicColors,
            )
        }
    }
}

@Composable
private fun StorageSection(
    downloadLocation: String,
    cacheSize: String,
    onDownloadLocationClick: () -> Unit,
    onClearCacheClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.storage),
        subtitle = stringResource(R.string.storage_subtitle),
    ) {
        SettingsPreference(
            icon = { Icon(Icons.Outlined.Download, contentDescription = null) },
            title = stringResource(R.string.download_location),
            subtitle = downloadLocation,
            onClick = onDownloadLocationClick,
        )

        SettingsAction(
            icon = { Icon(Icons.Outlined.Cached, contentDescription = null) },
            title = stringResource(R.string.wallpaper_cache),
            subtitle = cacheSize,
            actionLabel = stringResource(R.string.clear),
            onAction = onClearCacheClick,
        )
    }
}

@Composable
private fun SystemSection(
    onNotificationsClick: () -> Unit,
    onUnknownAppsClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.system),
        subtitle = stringResource(R.string.system_subtitle),
    ) {
        SettingsPreference(
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
            title = stringResource(R.string.notifications),
            subtitle = stringResource(R.string.notifications_subtitle),
            onClick = onNotificationsClick,
        )

        SettingsPreference(
            icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
            title = stringResource(R.string.install_unknown_apps),
            subtitle = stringResource(R.string.install_unknown_apps_subtitle),
            onClick = onUnknownAppsClick,
        )
    }
}

@Composable
private fun AboutSection(
    onAboutClick: () -> Unit,
    onOpenSourceClick: () -> Unit,
) {
    SettingsSection(
        title = stringResource(R.string.about),
        subtitle = stringResource(R.string.about_section_subtitle),
    ) {
        SettingsPreference(
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            title = stringResource(R.string.about_item),
            subtitle = stringResource(R.string.about_item_subtitle),
            onClick = onAboutClick,
        )

        SettingsPreference(
            icon = { Icon(Icons.Outlined.Code, contentDescription = null) },
            title = stringResource(R.string.open_source),
            subtitle = stringResource(R.string.open_source_subtitle),
            onClick = onOpenSourceClick,
        )
    }
}

@Composable
private fun AboutDialog(
    appIcon: androidx.compose.ui.graphics.ImageBitmap,
    onCheckForUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    bitmap = appIcon,
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(22.dp)),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(28.dp))
                Text(
                    stringResource(R.string.about_dialog_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
                Text(
                    text = stringResource(
                        R.string.version_value,
                        BuildConfig.VERSION_NAME,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onCheckForUpdate),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(28.dp))
                Text(
                    stringResource(R.string.made_with),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}


@Composable
private fun UpdateAvailableDialog(
    update: AppUpdate,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.update_available),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.update_version_available, update.version),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (update.notes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(stringResource(R.string.whats_new), fontWeight = FontWeight.SemiBold)
                        update.notes.take(6).forEach { note ->
                            Text(
                                "• $note",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.later))
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text(stringResource(R.string.download_update), fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
    )
}

private fun installDownloadedUpdate(context: Context) {
    context.startActivity(
        Intent(
            context,
            MainActivity::class.java,
        ).apply {
            action = UpdateManager.ACTION_INSTALL_UPDATE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
    )
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column {
            Column(
                modifier = Modifier.padding(
                    start = 28.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 4.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            content()
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SettingsPreference(
    icon: (@Composable () -> Unit)?,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsIcon(icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    icon: (@Composable () -> Unit)?,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsIcon(icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsAction(
    icon: (@Composable () -> Unit)?,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsIcon(icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        TextButton(
            onClick = onAction,
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            Text(actionLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsIcon(icon: (@Composable () -> Unit)?) {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        icon?.invoke()
    }
}

@Composable
private fun ThemeDialog(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme)) },
        text = {
            Column {
                ThemeMode.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option) }
                            .padding(
                                vertical = dimensionResource(R.dimen.settings_dialog_row_padding),
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { onSelected(option) },
                        )
                        Text(
                            option.label(),
                            Modifier.padding(
                                start = dimensionResource(R.dimen.settings_dialog_label_padding),
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
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

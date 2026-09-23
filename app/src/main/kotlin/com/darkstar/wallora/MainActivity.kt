package com.darkstar.wallora

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.data.UpdateManager
import com.darkstar.wallora.data.UpdateNotificationHelper
import com.darkstar.wallora.ui.theme.WalloraTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UpdateNotificationHelper.createChannel(this)
        handleUpdateIntent(intent)
        setContent {
            val preferences = remember { PreferencesStore(applicationContext) }
            WalloraTheme(themeMode = preferences.themeMode, dynamicColors = preferences.dynamicColors) { WalloraApp(preferences) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent); setIntent(intent); handleUpdateIntent(intent)
    }

    private fun handleUpdateIntent(intent: Intent?) {
        when (intent?.action) {
            UpdateManager.ACTION_DOWNLOAD_UPDATE -> {
                UpdateManager.enqueueDownload(this)
                Toast.makeText(this, R.string.update_download_started, Toast.LENGTH_SHORT).show()
            }
            UpdateManager.ACTION_INSTALL_UPDATE -> installDownloadedUpdate()
        }
    }

    private fun installDownloadedUpdate() {
        val update = UpdateManager.getDownloadedUpdate(this) ?: run { Toast.makeText(this, R.string.update_file_missing, Toast.LENGTH_LONG).show(); return }
        val apk = File(File(filesDir, "updates"), update.fileName)
        if (!apk.isFile) { Toast.makeText(this, R.string.update_file_missing, Toast.LENGTH_LONG).show(); return }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            Toast.makeText(this, R.string.allow_install_updates, Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${packageName}")))
            return
        }
        try {
            val apkUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", apk)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                clipData = ClipData.newRawUri("APK", apkUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (installIntent.resolveActivity(packageManager) == null) { Toast.makeText(this, R.string.update_install_failed, Toast.LENGTH_LONG).show(); return }
            startActivity(installIntent)
        } catch (_: Exception) { Toast.makeText(this, R.string.update_install_failed, Toast.LENGTH_LONG).show() }
    }
}
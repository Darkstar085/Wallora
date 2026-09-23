package com.darkstar.wallora.data

import android.content.Context
import com.darkstar.wallora.BuildConfig
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppUpdate(
    val tag: String, val version: String, val name: String, val notes: List<String>,
    val downloadUrl: String, val fileName: String, val size: Long, val digest: String?,
    val releaseDate: String?,
)

object UpdateManager {
    const val ACTION_DOWNLOAD_UPDATE = "com.darkstar.wallora.DOWNLOAD_UPDATE"
    const val ACTION_INSTALL_UPDATE = "com.darkstar.wallora.INSTALL_UPDATE"
    private const val RELEASE_API = "https://api.github.com/repos/Darkstar085/Wallora/releases/latest"
    private const val PREFS = "app_updates"
    private const val KEY_TAG = "pending_tag"
    private const val KEY_VERSION = "pending_version"
    private const val KEY_NAME = "pending_name"
    private const val KEY_NOTES = "pending_notes"
    private const val KEY_URL = "pending_url"
    private const val KEY_FILE = "pending_file"
    private const val KEY_SIZE = "pending_size"
    private const val KEY_DIGEST = "pending_digest"
    private const val KEY_DATE = "pending_release_date"
    private const val KEY_NOTIFIED = "last_notified_tag"
    private const val CHECK_WORK = "release_update_check"

    suspend fun checkForUpdate(): Result<AppUpdate?> = runCatching { withContext(Dispatchers.IO) { findLatestUpdate() } }

    suspend fun findLatestUpdate(): AppUpdate? {
        val request = Request.Builder().url(RELEASE_API)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "Wallora/${BuildConfig.VERSION_NAME}").build()
        NetworkClient.client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("GitHub API returned HTTP ${response.code}")
            val release = JSONObject(body)
            if (release.optBoolean("draft") || release.optBoolean("prerelease")) return null
            val version = release.optString("tag_name").removePrefix("v").trim()
            check(version.isNotBlank()) { "GitHub release version is missing" }
            if (!isNewerVersion(BuildConfig.VERSION_NAME, version)) return null
            val assets = release.optJSONArray("assets")
            var name: String? = null; var url: String? = null; var size = 0L; var digest: String? = null
            if (assets != null) for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val assetName = asset.optString("name")
                if (assetName.endsWith(".apk", true) || asset.optString("content_type").equals("application/vnd.android.package-archive", true)) {
                    name = assetName.takeIf { it.isNotBlank() }
                    url = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                    size = asset.optLong("size", 0L)
                    digest = asset.optString("digest").takeIf { it.isNotBlank() }
                    break
                }
            }
            return AppUpdate(
                tag = release.optString("tag_name").ifBlank { "v$version" },
                version = version,
                name = release.optString("name").ifBlank { "Wallora $version" },
                notes = release.optString("body").lineSequence().map(String::trim)
                    .filter { it.startsWith("- ") }.map { it.removePrefix("- ").trim() }
                    .filter(String::isNotBlank).distinct().toList(),
                downloadUrl = checkNotNull(url) { "GitHub release does not contain an APK asset" },
                fileName = checkNotNull(name) { "GitHub release APK name is missing" },
                size = size, digest = digest,
                releaseDate = release.optString("published_at").takeIf { it.isNotBlank() }?.let(::formatDate),
            )
        }
    }

    fun savePendingUpdate(context: Context, update: AppUpdate) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_TAG, update.tag).putString(KEY_VERSION, update.version)
            .putString(KEY_NAME, update.name).putString(KEY_NOTES, update.notes.joinToString("\n"))
            .putString(KEY_URL, update.downloadUrl).putString(KEY_FILE, update.fileName)
            .putLong(KEY_SIZE, update.size).putString(KEY_DIGEST, update.digest)
            .putString(KEY_DATE, update.releaseDate).apply()
    }

    fun getPendingUpdate(context: Context): AppUpdate? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val tag = prefs.getString(KEY_TAG, null) ?: return null
        val version = prefs.getString(KEY_VERSION, null) ?: return null
        if (!isNewerVersion(currentVersion(context), version)) { clearPendingUpdate(context); return null }
        val name = prefs.getString(KEY_NAME, null) ?: return null
        val url = prefs.getString(KEY_URL, null) ?: return null
        val file = prefs.getString(KEY_FILE, null) ?: return null
        val notes = prefs.getString(KEY_NOTES, "").orEmpty().lineSequence().filter { it.isNotBlank() }.toList()
        return AppUpdate(tag, version, name, notes, url, file, prefs.getLong(KEY_SIZE, 0L), prefs.getString(KEY_DIGEST, null), prefs.getString(KEY_DATE, null))
    }

    fun getDownloadedUpdate(context: Context): AppUpdate? {
        val update = getPendingUpdate(context) ?: return null
        val file = File(File(context.filesDir, "updates"), update.fileName)
        return update.takeIf { file.isFile && file.length() > 0L }
    }

    fun clearPendingUpdate(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_FILE, null)?.let { File(File(context.filesDir, "updates"), it).delete() }
        prefs.edit().remove(KEY_TAG).remove(KEY_VERSION).remove(KEY_NAME).remove(KEY_NOTES)
            .remove(KEY_URL).remove(KEY_FILE).remove(KEY_SIZE).remove(KEY_DIGEST).remove(KEY_DATE).apply()
    }

    fun wasNotified(context: Context, tag: String) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NOTIFIED, null) == tag
    fun markNotified(context: Context, tag: String) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_NOTIFIED, tag).apply() }

    fun enqueuePeriodicCheck(context: Context) {
        val constraints = androidx.work.Constraints.Builder().setRequiredNetworkType(androidx.work.NetworkType.CONNECTED).setRequiresBatteryNotLow(true).build()
        val request = androidx.work.PeriodicWorkRequestBuilder<UpdateCheckWorker>(6, java.util.concurrent.TimeUnit.HOURS).setConstraints(constraints).build()
        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(CHECK_WORK, androidx.work.ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun enqueueDownload(context: Context, update: AppUpdate? = null) {
        val target = update ?: getPendingUpdate(context) ?: return
        val data = androidx.work.Data.Builder().putString(UpdateDownloadWorker.KEY_TAG, target.tag)
            .putString(UpdateDownloadWorker.KEY_URL, target.downloadUrl).putString(UpdateDownloadWorker.KEY_FILE, target.fileName)
            .apply { target.digest?.let { putString(UpdateDownloadWorker.KEY_DIGEST, it) } }.build()
        val request = androidx.work.OneTimeWorkRequestBuilder<UpdateDownloadWorker>().setInputData(data).build()
        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork("update_download_${target.tag}", androidx.work.ExistingWorkPolicy.KEEP, request)
    }

    private fun currentVersion(context: Context) = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val a = current.removePrefix("v").split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val b = latest.removePrefix("v").split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) { val x = a.getOrElse(i) { 0 }; val y = b.getOrElse(i) { 0 }; if (x != y) return y > x }
        return false
    }
    private fun formatDate(value: String) = runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())) }.getOrNull()
}
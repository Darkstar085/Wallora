package com.darkstar.wallora.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.darkstar.wallora.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class WallpaperDownloader(private val context: Context, private val client: OkHttpClient = OkHttpClient()) {
    suspend fun download(wallpaper: Wallpaper, customTreeUri: String?): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            customTreeUri?.let { downloadToTree(resolver, Uri.parse(it), wallpaper) }
                ?: downloadToPictures(resolver, wallpaper)
                ?: error("Unable to create a download destination")
        }
    }

    private fun downloadToPictures(resolver: ContentResolver, wallpaper: Wallpaper): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, wallpaper.filename)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType(wallpaper.format))
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wallora")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        return try {
            downloadBytes(wallpaper.url).use { input -> resolver.openOutputStream(uri)?.use { output -> input.copyTo(output) } ?: error("Unable to open output") }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        } catch (error: Throwable) { resolver.delete(uri, null, null); throw error }
    }

    private fun downloadToTree(resolver: ContentResolver, treeUri: Uri, wallpaper: Wallpaper): Uri? {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        val uri = DocumentsContract.createDocument(resolver, documentUri, mimeType(wallpaper.format), wallpaper.filename) ?: return null
        return try {
            downloadBytes(wallpaper.url).use { input -> resolver.openOutputStream(uri)?.use { output -> input.copyTo(output) } ?: error("Unable to open output") }
            uri
        } catch (error: Throwable) { DocumentsContract.deleteDocument(resolver, uri); throw error }
    }

    private fun downloadBytes(url: String) = client.newCall(Request.Builder().url(url).build()).execute().let { response ->
        check(response.isSuccessful) { "Wallpaper download failed: HTTP ${response.code}" }
        response.body?.byteStream() ?: error("Wallpaper download returned an empty response")
    }

    private fun mimeType(format: String): String = when (format.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "image/*"
    }
}

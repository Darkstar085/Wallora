package com.darkstar.wallora.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.darkstar.wallora.R

object NotificationHelper {
    private const val CHANNEL_ID = "wallora_downloads"
    private const val CHANNEL_NAME = "Downloads"

    fun showDownloadComplete(context: Context, filename: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val fileUri = findDownloadedFile(context, filename)
        val galleryPendingIntent = fileUri?.let { createViewPendingIntent(context, it, filename) }
        val sharePendingIntent = fileUri?.let { createSharePendingIntent(context, it, filename) }
        val bitmap = fileUri?.let { uri -> runCatching { context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) }.getOrNull() }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Wallpaper downloaded")
            .setContentText(filename)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        galleryPendingIntent?.let { builder.setContentIntent(it) }
        sharePendingIntent?.let {
            builder.addAction(android.R.drawable.ic_menu_share, "Share", it)
        }
        galleryPendingIntent?.let {
            builder.addAction(android.R.drawable.ic_menu_view, "Open in Gallery", it)
        }
        bitmap?.let {
            builder.setLargeIcon(it)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(it)
                    .setBigContentTitle("Wallpaper downloaded")
                    .setSummaryText(filename)
            )
        }

        manager.notify(filename.hashCode(), builder.build())
    }

    private fun createViewPendingIntent(context: Context, uri: Uri, filename: String): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return PendingIntent.getActivity(
            context,
            filename.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSharePendingIntent(context: Context, uri: Uri, filename: String): PendingIntent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share wallpaper")
        return PendingIntent.getActivity(
            context,
            filename.hashCode() + 1,
            chooser,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun findDownloadedFile(context: Context, filename: String): Uri? {
        val customTreeUri = PreferencesStore(context).downloadLocationUri
        return customTreeUri?.let { findInTree(context, Uri.parse(it), filename) }
            ?: findInMediaStore(context, filename)
    }

    private fun findInMediaStore(context: Context, filename: String): Uri? {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Images.Media.DISPLAY_NAME} = ? AND ${MediaStore.Images.Media.RELATIVE_PATH} = ?",
            arrayOf(filename, Environment.DIRECTORY_PICTURES + "/Wallora/"),
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)).toString())
                    .build()
            }
        }
        return null
    }

    private fun findInTree(context: Context, treeUri: Uri, filename: String): Uri? {
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull() ?: return null
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            while (cursor.moveToNext()) {
                if (nameColumn >= 0 && idColumn >= 0 && cursor.getString(nameColumn) == filename) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(idColumn))
                }
            }
        }
        return null
    }
}

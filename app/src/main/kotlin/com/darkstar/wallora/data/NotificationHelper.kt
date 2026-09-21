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

    fun showDownloadComplete(context: Context, uri: Uri, filename: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        val galleryPendingIntent = createViewPendingIntent(context, uri, filename)
        val sharePendingIntent = createSharePendingIntent(context, uri, filename)
        val bitmap = runCatching { context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream) }.getOrNull()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Wallpaper downloaded")
            .setContentText(filename)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        builder.setContentIntent(galleryPendingIntent)
        builder.addAction(android.R.drawable.ic_menu_share, "Share", sharePendingIntent)
        builder.addAction(android.R.drawable.ic_menu_view, "Open in Gallery", galleryPendingIntent)
        bitmap?.let {
            builder.setLargeIcon(it)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(it)
                    .setBigContentTitle("Wallpaper downloaded")
                    .setSummaryText(filename)
            )
        }

        manager.notify(filename.hashCode() * 31 + uri.hashCode(), builder.build())
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

}

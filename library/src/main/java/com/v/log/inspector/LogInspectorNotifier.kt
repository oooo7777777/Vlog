package com.v.log.inspector

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.v.log.R

object LogInspectorNotifier {

    private const val CHANNEL_ID = "vlog_inspector"
    private const val NOTIFICATION_ID = 2004

    fun setup(context: Context, enable: Boolean) {
        if (!enable) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            return
        }
        createChannel(context)
        update(context)
    }

    fun update(context: Context) {
        if (!LogInspectorStore.isEnabled()) return
        createChannel(context)
        val logs = LogInspectorStore.snapshot()
        val latest = logs.lastOrNull()
        val content = if (latest == null) {
            context.getString(R.string.vlog_notification_empty)
        } else {
            latest.levelName + " " + latest.tag + ": " + latest.message.lineSequence().firstOrNull().orEmpty()
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setColor(Color.BLACK)
            .setContentTitle(
                context.getString(
                    R.string.vlog_notification_title,
                    logs.size
                )
            )
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(createContentIntent(context))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, LogViewerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(context, 1, intent, flags)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.vlog_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.vlog_notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }
}

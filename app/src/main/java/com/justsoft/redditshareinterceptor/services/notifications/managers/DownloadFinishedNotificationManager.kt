package com.justsoft.redditshareinterceptor.services.notifications.managers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.justsoft.redditshareinterceptor.R
import com.justsoft.redditshareinterceptor.components.services.UniversalProcessorForegroundService
import com.justsoft.redditshareinterceptor.services.notifications.NotificationService

class DownloadFinishedNotificationManager(
    private val context: Context,
    notificationService: NotificationService,
    notificationChannelId: String
) : AbstractNotificationManager(context, notificationService, notificationChannelId) {
    override val notificationId: Int
        get() = 457

    fun notifyDownloadFinished(action: Intent) = notify(buildNotification(action))

    private fun buildNotification(action: Intent?): Notification {
        val notificationBuilder = createNotificationBuilder()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH

        action?.let { notificationBuilder.addAction(createSendAction(it)) }
            ?: Log.e("DownloadFinishedNotificationManager", "Built notification without action")

        return notificationBuilder
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(context.getString(R.string.notification_download_finished_title))
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColorized(true)
            .setColor(context.getColor(R.color.colorPrimary))
            .build()
    }

    private fun createSendAction(action: Intent) = NotificationCompat.Action.Builder(
        R.drawable.ic_send,
        context.getString(R.string.notification_download_finished_action_send),
        PendingIntent.getBroadcast(
            context,
            UniversalProcessorForegroundService.DOWNLOAD_FINISHED_NOTIFICATION_ID,
            action,
            PendingIntent.FLAG_ONE_SHOT
        )
    ).build()

    override fun buildNotification(): Notification = buildNotification(null)
}
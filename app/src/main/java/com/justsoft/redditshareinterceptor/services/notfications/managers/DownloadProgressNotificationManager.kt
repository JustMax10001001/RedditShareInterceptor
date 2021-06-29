package com.justsoft.redditshareinterceptor.services.notfications.managers

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import com.justsoft.redditshareinterceptor.R
import com.justsoft.redditshareinterceptor.services.notfications.NotificationService

class DownloadProgressNotificationManager internal constructor(
    private val context: Context,
    notificationService: NotificationService,
    notificationChannelId: String
) : AbstractNotificationManager(
    context,
    notificationService, notificationChannelId
) {
    override val notificationId: Int
        get() = 456

    override fun buildNotification(): Notification =
        buildNotification(0, context.getString(R.string.processing_media_state_starting))

    private fun buildNotification(progress: Int, stateDescription: String): Notification {
        val notificationBuilder = createNotificationBuilder()

        if (progress >= 0)
            notificationBuilder.setProgress(100, progress, false)

        if (progress < 0 || progress >= 100)
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
        else
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationBuilder.setColorized(true)

        return notificationBuilder
            .setContentTitle(context.getString(R.string.processing_media))
            .setContentText(stateDescription)
            .setColor(context.getColor(R.color.colorPrimary))
            .build()
    }

    fun updateDownloadState(progress: Int, @StringRes stateDescription: Int) =
        updateDownloadState(progress, context.getString(stateDescription))

    fun updateDownloadState(progress: Int, stateDescription: String) =
        notify(buildNotification(progress, stateDescription))
}
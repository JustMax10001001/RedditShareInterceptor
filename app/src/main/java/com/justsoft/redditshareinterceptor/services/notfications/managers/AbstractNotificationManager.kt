package com.justsoft.redditshareinterceptor.services.notfications.managers

import android.app.Notification
import android.app.Service
import android.content.Context
import android.os.Build
import com.justsoft.redditshareinterceptor.services.notfications.NotificationService

abstract class AbstractNotificationManager(
    private val context: Context,
    private val notificationService: NotificationService,
    private val notificationChannelId: String
) {
    abstract val notificationId: Int

    protected fun createNotificationBuilder(): Notification.Builder =
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(context, notificationChannelId)
        else
            Notification.Builder(context)

    protected abstract fun buildNotification(): Notification

    fun attachServiceToNotification(service: Service) =
        service.startForeground(notificationId, buildNotification())

    protected fun notify(notification: Notification) =
        notificationService.notify(notificationId, notification)

    fun cancel() = notificationService.cancel(notificationId)

    fun detachServiceFromNotification(service: Service, removeNotification: Boolean) =
        service.stopForeground(removeNotification)
}
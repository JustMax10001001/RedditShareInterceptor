package com.justsoft.redditshareinterceptor.services.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationService {

    private val notificationManager = NotificationManagerCompat.from(context)

    override fun registerNotificationChannel(notificationChannel: NotificationChannel) =
        notificationManager.createNotificationChannel(notificationChannel)

    override fun notify(notificationId: Int, notification: Notification) =
        notificationManager.notify(notificationId, notification)

    override fun cancel(notificationId: Int) =
        notificationManager.cancel(notificationId)
}
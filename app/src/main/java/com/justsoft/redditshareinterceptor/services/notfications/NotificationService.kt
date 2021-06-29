package com.justsoft.redditshareinterceptor.services.notfications

import android.app.Notification
import android.app.NotificationChannel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

interface NotificationService {
    fun registerNotificationChannel(notificationChannel: NotificationChannel)

    fun notify(notificationId: Int, notification: Notification)

    fun cancel(notificationId: Int)
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NotificationServiceModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: AndroidNotificationService): NotificationService
}
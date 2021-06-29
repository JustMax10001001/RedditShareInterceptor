package com.justsoft.redditshareinterceptor.services.notifications.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.justsoft.redditshareinterceptor.R
import com.justsoft.redditshareinterceptor.services.notifications.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NotificationManagersModule {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDownloadInProgressChannel(context: Context): NotificationChannel =
        NotificationChannel(
            ONGOING_DOWNLOAD_CHANNEL_ID,
            context.getString(R.string.ongoing_media_download),
            NotificationManager.IMPORTANCE_LOW
        )

    @Provides
    @Singleton
    fun provideDownloadProgressNotificationManager(
        @ApplicationContext context: Context,
        notificationService: NotificationService
    ): DownloadProgressNotificationManager {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationService.registerNotificationChannel(getDownloadInProgressChannel(context))

        return DownloadProgressNotificationManager(
            context,
            notificationService,
            ONGOING_DOWNLOAD_CHANNEL_ID
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDownloadFinishedChannel(context: Context): NotificationChannel =
        NotificationChannel(
            DOWNLOAD_FINISHED_CHANNEL_ID,
            context.getString(R.string.media_download_finished),
            NotificationManager.IMPORTANCE_HIGH
        )

    @Provides
    @Singleton
    fun provideDownloadFinishedNotificationManager(
        @ApplicationContext context: Context,
        notificationService: NotificationService
    ): DownloadFinishedNotificationManager {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationService.registerNotificationChannel(getDownloadFinishedChannel(context))

        return DownloadFinishedNotificationManager(
            context,
            notificationService,
            DOWNLOAD_FINISHED_CHANNEL_ID
        )
    }

    companion object {
        private const val ONGOING_DOWNLOAD_CHANNEL_ID = "ongoing_download"
        private const val DOWNLOAD_FINISHED_CHANNEL_ID = "download_finished"
    }
}
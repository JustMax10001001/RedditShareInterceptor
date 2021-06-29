package com.justsoft.redditshareinterceptor.components.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.justsoft.redditshareinterceptor.providers.media.IntentProvider
import com.justsoft.redditshareinterceptor.services.notifications.managers.DownloadFinishedNotificationManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class NotificationBroadcastReceiver : BroadcastReceiver() {

    private lateinit var mDownloadFinishedNotificationManager: DownloadFinishedNotificationManager
    private lateinit var mIntentProvider: IntentProvider

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: throw IllegalArgumentException("Context can not be null")
        intent ?: throw IllegalArgumentException("Intent can not be null")

        injectDependencies(context)

        mDownloadFinishedNotificationManager.cancel()
        Log.d(LOG_TAG, "Cancelled notification")

        val mediaPost = mIntentProvider.parseInternalMediaIntent(intent)
        val shareMediaIntent = mIntentProvider.provideMediaShareIntent(mediaPost)

        context.startActivity(shareMediaIntent)
    }

    private fun injectDependencies(context: Context) {
        EntryPointAccessors.fromApplication(
            context,
            NotificationBroadcastReceiverEntryPoint::class.java
        ).apply {
            mIntentProvider = mediaIntentProvider()
            mDownloadFinishedNotificationManager = downloadFinishedNotificationManager()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationBroadcastReceiverEntryPoint {
        fun downloadFinishedNotificationManager(): DownloadFinishedNotificationManager
        fun mediaIntentProvider(): IntentProvider
    }

    companion object {
        private const val LOG_TAG = "SendMediaBroadcast"
    }
}
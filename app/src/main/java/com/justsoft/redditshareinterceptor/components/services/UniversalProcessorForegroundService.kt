package com.justsoft.redditshareinterceptor.components.services

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.R
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.ACTION_SHARE_MEDIA
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.FLAG_MULTIPLE_MEDIA
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.FLAG_NO_MEDIA
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.FLAG_SINGLE_MEDIA
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.KEY_MEDIA_CAPTION
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.KEY_MEDIA_FLAG
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.KEY_MEDIA_SINGLE_URI
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.KEY_MEDIA_URI_LIST
import com.justsoft.redditshareinterceptor.SendNotificationBroadcastReceiver.Companion.KEY_MIME_TYPE
import com.justsoft.redditshareinterceptor.UniversalUrlProcessor
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.*
import com.justsoft.redditshareinterceptor.utils.request.VolleyRequestHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.stream.Collectors

@AndroidEntryPoint
class UniversalProcessorForegroundService : Service() {

    private val mHandler = Handler(Looper.getMainLooper())
    private val mBackgroundExecutor =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "UUFSBackgroundThread")
        }

    private val mUniversalUrlProcessor: UniversalUrlProcessor by lazy {
        UniversalUrlProcessor(
            VolleyRequestHelper(applicationContext),
            this::getUriForContentType,
            this::getInternalFileByContentType,
            this::openStreamForUri,
        )
    }

    // LIFECYCLE METHODS

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate()")
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            buildNotificationChannels()
        }

        mUniversalUrlProcessor.finished { result ->
            if (result.processingSuccessful)
                onResult(result)
            else
                onError(result.cause)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand()")

        intent ?: throw IllegalArgumentException("intent is null")

        if (intent.action == ACTION_PROCESS_URL) {
            startForeground(DOWNLOADING_NOTIFICATION_ID, buildProgressNotification())
            mBackgroundExecutor.submit {
                val url = intent.extras?.get(Intent.EXTRA_TEXT).toString()
                FirebaseCrashlytics.getInstance().setCustomKey("url", url)
                mUniversalUrlProcessor.handleUrl(url, this::onProgress)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mBackgroundExecutor.shutdown()
        stopForeground(true)

        super.onDestroy()

        Log.d(LOG_TAG, "onDestroy()")
    }

    // END LIFECYCLE METHODS

    // PROCESSOR HANDLER EVENTS

    private fun onError(err: Throwable) {
        Log.e(LOG_TAG, "Error processing post", err)
        FirebaseCrashlytics.getInstance().recordException(err)

        longToast(getString(R.string.error_processing_url))
        updateDownloadNotification(
            -1,
            getString(R.string.error_processing_url_extended).format(err.message)
        )
        stopForeground(false)
    }

    private fun onResult(processingResult: ProcessingResult) {
        Log.d(LOG_TAG, "Processing succeeded")
        executeOnMainThread {
            updateDownloadNotification(100, R.string.processing_media_state_processed)
            val broadcastIntent = prepareBroadcastReceiverIntent(processingResult)
            if (processingResult.processingTime <= 5000) {
                Log.d(LOG_TAG, "Processing completed in under 5 seconds, using sendBroadcast()")
                sendBroadcast(broadcastIntent)
            } else {
                Log.d(LOG_TAG, "Processing completed in over 5 seconds, sending notification")
                notify(
                    DOWNLOAD_FINISHED_NOTIFICATION_ID,
                    buildProcessingFinishedNotification(broadcastIntent)
                )
            }
        }
        stopForeground(true)
        cancelNotification(DOWNLOADING_NOTIFICATION_ID)
    }

    private var lastProgressUpdate: Long = 0
    private var lastStatusId: Int = 0

    private fun onProgress(processingProgress: ProcessingProgress) {
        if (lastStatusId == processingProgress.statusTextResourceId
            && System.currentTimeMillis() - lastProgressUpdate < 250
        )
            return
        lastProgressUpdate = System.currentTimeMillis()
        lastStatusId = processingProgress.statusTextResourceId
        executeOnMainThread {
            updateDownloadNotification(
                processingProgress.overallProgress,
                processingProgress.statusTextResourceId
            )
        }
    }

    // END PROCESSOR HANDLER EVENTS

    // INTENT CREATION

    private fun prepareBroadcastReceiverIntent(processingResult: ProcessingResult): Intent {
        return Intent(this, SendNotificationBroadcastReceiver::class.java).apply {
            action = ACTION_SHARE_MEDIA

            val mediaInfo = processingResult.mediaInfo

            putExtra(KEY_MEDIA_CAPTION, mediaInfo.caption)
            putExtra(KEY_MIME_TYPE, getMimeForContentType(mediaInfo.mediaContentType))
            putExtra(
                KEY_MEDIA_FLAG, when (mediaInfo.mediaContentType) {
                    GALLERY -> FLAG_MULTIPLE_MEDIA
                    TEXT -> FLAG_NO_MEDIA
                    else -> FLAG_SINGLE_MEDIA
                }
            )
            when (mediaInfo.mediaContentType) {
                GALLERY -> putExtra(
                    KEY_MEDIA_URI_LIST,
                    ArrayList(mediaInfo.mediaDownloadList.stream().map { it.metadata.uri }
                        .collect(Collectors.toList()))
                )
                TEXT -> {
                }
                else -> putExtra(
                    KEY_MEDIA_SINGLE_URI,
                    getUriForContentType(processingResult.mediaInfo.mediaContentType, 0)
                )
            }
        }
    }

    // END INTENT CREATION

    // NOTIFICATIONS

    private val mNotificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(applicationContext)
    }

    @Suppress("SameParameterValue")
    private fun cancelNotification(id: Int) = mNotificationManager.cancel(id)

    @Suppress("SameParameterValue")
    private fun notify(id: Int, notification: Notification) =
        mNotificationManager.notify(id, notification)

    private fun updateDownloadNotification(
        progress: Int = 0,
        statusTextResId: Int = R.string.processing_media_state_starting
    ) {
        mNotificationManager.notify(
            DOWNLOADING_NOTIFICATION_ID,
            buildProgressNotification(progress, statusTextResId)
        )
    }

    @Suppress("SameParameterValue")
    private fun updateDownloadNotification(
        progress: Int = 0,
        statusText: String
    ) {
        mNotificationManager.notify(
            DOWNLOADING_NOTIFICATION_ID,
            buildProgressNotification(progress, statusText)
        )
    }

    private fun buildProcessingFinishedNotification(action: Intent): Notification {
        val notificationBuilder = notificationBuilder(DOWNLOAD_FINISHED_CHANNEL_ID)

        if (VERSION.SDK_INT < VERSION_CODES.O)
            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH

        notificationBuilder
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(getString(R.string.notification_download_finished_title))
            .setAutoCancel(true)
            .setDefaults(DEFAULT_ALL)
            .setColorized(true)
            .setColor(getColor(R.color.colorPrimary))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_send,
                    getString(R.string.notification_download_finished_action_send),
                    PendingIntent.getBroadcast(
                        this,
                        DOWNLOAD_FINISHED_NOTIFICATION_ID,
                        action,
                        PendingIntent.FLAG_ONE_SHOT
                    )
                ).build()
            )

        return notificationBuilder.build()
    }

    private fun buildProgressNotification(
        progress: Int = 0,
        statusTextResId: Int = R.string.processing_media_state_starting
    ): Notification = buildProgressNotification(progress, getString(statusTextResId))

    private fun buildProgressNotification(
        progress: Int = 0,
        statusText: String
    ): Notification {
        val notificationBuilder = notificationBuilder(ONGOING_DOWNLOAD_CHANNEL_ID)

        if (progress >= 0)
            notificationBuilder.setProgress(100, progress, false)

        if (progress < 0 || progress >= 100)
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
        else
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download)

        return notificationBuilder
            .setContentTitle(getString(R.string.processing_media))
            .setContentText(statusText)
            .setColorized(true)
            .setColor(getColor(R.color.colorPrimary))
            .build()
    }

    private fun notificationBuilder(channelId: String): NotificationCompat.Builder {
        @Suppress("DEPRECATION")
        return if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationCompat.Builder(applicationContext, channelId)
        } else
            NotificationCompat.Builder(applicationContext)
    }

    @RequiresApi(VERSION_CODES.O)
    private fun buildNotificationChannels() {
        val downloadChannel = NotificationChannel(
            ONGOING_DOWNLOAD_CHANNEL_ID,
            getString(R.string.ongoing_media_download),
            NotificationManager.IMPORTANCE_LOW
        )
        val downloadFinishedChannel = NotificationChannel(
            DOWNLOAD_FINISHED_CHANNEL_ID,
            getString(R.string.media_download_finished),
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(downloadChannel)
        notificationManager.createNotificationChannel(downloadFinishedChannel)
    }

    // END NOTIFICATIONS

    // UTILITY METHODS

    private fun longToast(message: String) {
        executeOnMainThread {
            Toast.makeText(
                applicationContext, message, Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun executeOnMainThread(exec: () -> Unit) {
        mHandler.post(exec)
    }

    private fun openStreamForUri(uri: Uri): OutputStream =
        contentResolver.openOutputStream(uri)!!

    private fun getUriForContentType(mediaContentType: MediaContentType, mediaIndex: Int): Uri =
        getInternalFileUri(getFileNameForContentType(mediaContentType, mediaIndex))

    private fun getInternalFileByContentType(
        mediaContentType: MediaContentType,
        index: Int = 0
    ): File {
        return File(filesDir, getFileNameForContentType(mediaContentType, index))
    }

    private fun getInternalFileUri(file: String): Uri {
        return FileProvider.getUriForFile(
            this,
            getString(R.string.provider_name),
            File(filesDir, file)
        )
    }

    private fun getFileNameForContentType(
        mediaContentType: MediaContentType,
        index: Int = 0
    ): String =
        (contentTypeToFileNameMap[mediaContentType]
            ?: error("No such key: $mediaContentType in Filename map")).format(index)

    private fun getMimeForContentType(mediaContentType: MediaContentType): String =
        contentTypeToMIME[mediaContentType] ?: error("No such key: $mediaContentType in MIME map")

    // END UTILITY METHODS

    companion object {
        private const val LOG_TAG = "UProcessorService"

        private const val ONGOING_DOWNLOAD_CHANNEL_ID = "ongoing_download"
        private const val DOWNLOAD_FINISHED_CHANNEL_ID = "download_finished"

        private const val DOWNLOADING_NOTIFICATION_ID = 456
        const val DOWNLOAD_FINISHED_NOTIFICATION_ID = 457

        const val ACTION_PROCESS_URL =
            "com.justsoft.redditshareinterceptor.action.PROCESS_REDDIT_URL"

        private val contentTypeToFileNameMap = mapOf(
            GIF to "gif.mp4",
            VIDEO to "video.mp4",
            IMAGE to "image.jpg",
            GALLERY to "image_%d.jpg",
            AUDIO to "audio.mp4",
            VIDEO_AUDIO to "video_w_audio.mp4"
        )

        private val contentTypeToMIME = mapOf(
            GIF to "video/*",
            VIDEO to "video/*",
            IMAGE to "image/*",
            GALLERY to "image/*",
            TEXT to "text/plain",
            AUDIO to "audio/mp4",
            VIDEO_AUDIO to "video/*",
        )
    }
}
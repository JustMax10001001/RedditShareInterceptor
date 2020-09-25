package com.justsoft.redditshareinterceptor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import androidx.core.content.FileProvider
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.util.VolleyRequestHelper
import java.io.File
import java.io.OutputStream
import java.util.concurrent.Executors

class UniversalProcessorForegroundService : Service() {

    private val mHandler = Handler(Looper.getMainLooper())
    private val mBackgroundExecutor =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "UUFSBackgroundThread")
        }

    private val mUniversalUrlProcessor: UniversalUrlProcessor by lazy {
        UniversalUrlProcessor(
            VolleyRequestHelper(Volley.newRequestQueue(applicationContext)),
            this::getUriForContentType,
            this::openStreamForUri
        )
    }

    // LIFECYCLE METHODS

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate()")

        mUniversalUrlProcessor.error(::onError)
        mUniversalUrlProcessor.result(::onResult)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand()")

        intent ?: throw IllegalArgumentException("intent is null")

        if (intent.action == ACTION_PROCESS_URL) {
            startForeground(DOWNLOADING_NOTIFICATION_ID, buildNotification())
            mBackgroundExecutor.submit {
                val url = intent.extras?.get(Intent.EXTRA_TEXT).toString()
                FirebaseCrashlytics.getInstance().setCustomKey("url", url)
                mUniversalUrlProcessor.handleUrl(url)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
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
        executeOnMainThread {
            updateDownloadNotification(100, R.string.media_processing_state_processed)
            startActivity(
                when (processingResult.contentType) {
                    MediaContentType.GALLERY -> prepareMediaMultipleIntent(
                        processingResult.caption,
                        processingResult.mediaUris
                    )
                    MediaContentType.TEXT -> prepareTextIntent(processingResult.caption)
                    else -> prepareMediaIntent(
                        processingResult.caption,
                        processingResult.contentType,
                        processingResult.mediaUris.first()
                    )
                }
            )
            stopForeground(true)
        }
    }

    // END PROCESSOR HANDLER EVENTS

    // INTENT CREATION

    private fun prepareMediaMultipleIntent(
        caption: String,
        uris: List<Uri>
    ): Intent {
        return prepareIntent(
            getMimeForContentType(MediaContentType.GALLERY),
            caption
        ).apply {
            action = Intent.ACTION_SEND_MULTIPLE

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
    }

    private fun prepareTextIntent(caption: String): Intent =
        prepareIntent(
            "text/plain",
            caption
        )

    private fun prepareMediaIntent(
        caption: String,
        mediaContentType: MediaContentType,
        mediaUri: Uri
    ): Intent =
        prepareIntent(
            getMimeForContentType(mediaContentType), caption
        ).putExtra(Intent.EXTRA_STREAM, mediaUri)

    private fun prepareIntent(mimeType: String, extraText: String): Intent =
        Intent().apply {
            action = Intent.ACTION_SEND
            type = mimeType

            putExtra(Intent.EXTRA_TEXT, extraText)

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    // END INTENT CREATION

    // NOTIFICATION

    private val mNotificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun updateDownloadNotification(
        progress: Int = 0,
        statusTextResId: Int = R.string.processing_media_state_starting
    ) {
        mNotificationManager.notify(
            DOWNLOADING_NOTIFICATION_ID,
            buildNotification(progress, statusTextResId)
        )
    }

    private fun updateDownloadNotification(
        progress: Int = 0,
        statusText: String
    ) {
        mNotificationManager.notify(
            DOWNLOADING_NOTIFICATION_ID,
            buildNotification(progress, statusText)
        )
    }

    private fun buildNotification(
        progress: Int = 0,
        statusText: String
    ): Notification {
        val notificationBuilder =
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                buildNotificationChannel()
                Notification.Builder(this, ONGOING_DOWNLOAD_CHANNEL_ID)
            } else
                Notification.Builder(this)

        if (progress >= 0)
            notificationBuilder.setProgress(100, progress, false)

        return notificationBuilder
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(getString(R.string.processing_media))
            .setContentText(statusText)
            .setOngoing(true)
            .build()
    }

    private fun buildNotification(
        progress: Int = 0,
        statusTextResId: Int = R.string.processing_media_state_starting
    ): Notification = buildNotification(progress, getString(statusTextResId))

    @RequiresApi(VERSION_CODES.O)
    private fun buildNotificationChannel() {
        val name = getString(R.string.ongoing_media_downlaoad)
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(ONGOING_DOWNLOAD_CHANNEL_ID, name, importance)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    // END NOTIFICATION

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
        private const val DOWNLOADING_NOTIFICATION_ID = 777

        const val ACTION_PROCESS_URL =
            "com.justsoft.redditshareinterceptor.action.PROCESS_REDDIT_URL"

        private val contentTypeToFileNameMap = mapOf(
            MediaContentType.GIF to "gif.mp4",
            MediaContentType.VIDEO to "video.mp4",
            MediaContentType.IMAGE to "image.jpg",
            MediaContentType.GALLERY to "image_%d.jpg",
        )

        private val contentTypeToMIME = mapOf(
            MediaContentType.GIF to "video/*",
            MediaContentType.VIDEO to "video/*",
            MediaContentType.IMAGE to "image/*",
            MediaContentType.GALLERY to "image/*",
        )
    }
}
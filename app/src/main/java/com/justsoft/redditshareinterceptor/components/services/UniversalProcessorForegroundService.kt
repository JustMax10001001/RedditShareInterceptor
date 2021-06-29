package com.justsoft.redditshareinterceptor.components.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.R
import com.justsoft.redditshareinterceptor.UniversalUrlProcessor
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.ProgressModel
import com.justsoft.redditshareinterceptor.providers.media.IntentProvider
import com.justsoft.redditshareinterceptor.providers.media.MediaPathProvider
import com.justsoft.redditshareinterceptor.services.notifications.managers.DownloadFinishedNotificationManager
import com.justsoft.redditshareinterceptor.services.notifications.managers.DownloadProgressNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class UniversalProcessorForegroundService : Service() {

    private val mHandler = Handler(Looper.getMainLooper())
    private val mBackgroundExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "UUFSBackgroundThread")
    }

    @Inject
    lateinit var mUniversalUrlProcessor: UniversalUrlProcessor

    @Inject
    lateinit var mDownloadProgressNotificationManager: DownloadProgressNotificationManager

    @Inject
    lateinit var mDownloadFinishedNotificationManager: DownloadFinishedNotificationManager

    @Inject
    lateinit var mMediaPathProvider: MediaPathProvider

    @Inject
    lateinit var mIntentProvider: IntentProvider

    // LIFECYCLE METHODS

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate()")

        mUniversalUrlProcessor.finished { result ->
            when {
                result.processingSuccessful -> onResult(result)
                else -> onError(result.cause)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand()")

        intent ?: error("intent is null")

        if (intent.action == ACTION_PROCESS_URL) {
            mDownloadProgressNotificationManager.attachServiceToNotification(this)

            mBackgroundExecutor.submit {
                val url = intent.extras?.get(Intent.EXTRA_TEXT).toString()

                runBlocking {
                    mUniversalUrlProcessor.handleUrl(url).collect(::onProgress)
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mBackgroundExecutor.shutdown()
        mDownloadProgressNotificationManager.detachServiceFromNotification(this, true)

        super.onDestroy()

        Log.d(LOG_TAG, "onDestroy()")
    }

    // END LIFECYCLE METHODS

    // PROCESSOR HANDLER EVENTS

    private fun onError(err: Throwable) {
        Log.e(LOG_TAG, "Error processing post", err)
        FirebaseCrashlytics.getInstance().recordException(err)

        longToast(getString(R.string.error_processing_url))
        mDownloadProgressNotificationManager.updateDownloadState(
            -1,
            getString(R.string.error_processing_url_extended, err.message)
        )

        mDownloadProgressNotificationManager.detachServiceFromNotification(this, false)
    }

    private fun onResult(processingResult: ProcessingResult) {
        Log.d(LOG_TAG, "Processing succeeded")
        executeOnMainThread {
            mDownloadProgressNotificationManager.updateDownloadState(
                100,
                R.string.processing_media_state_processed
            )

            val internalMediaIntent =
                mIntentProvider.provideInternalMediaIntent(processingResult.mediaPost)

            if (processingResult.processingTime >= 5000) {
                Log.d(LOG_TAG, "Processing completed in under 5 seconds, using sendBroadcast()")

                sendBroadcast(internalMediaIntent)
            } else {
                Log.d(LOG_TAG, "Processing completed in over 5 seconds, sending notification")

                mDownloadFinishedNotificationManager.notifyDownloadFinished(internalMediaIntent)
            }
        }
        mDownloadProgressNotificationManager.detachServiceFromNotification(this, true)
    }

    private var lastProgressUpdate: Long = 0
    private var lastStatusId: Int = 0

    private fun onProgress(progressModel: ProgressModel) {
        if (lastStatusId == progressModel.statusTextResourceId
            && System.currentTimeMillis() - lastProgressUpdate < 250
            && progressModel.overallProgress != 100
        )
            return
        lastProgressUpdate = System.currentTimeMillis()
        lastStatusId = progressModel.statusTextResourceId
        executeOnMainThread {
            mDownloadProgressNotificationManager.updateDownloadState(
                progressModel.overallProgress,
                progressModel.statusTextResourceId
            )
        }
    }

    // END PROCESSOR HANDLER EVENTS

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

    // END UTILITY METHODS

    companion object {
        private const val LOG_TAG = "UProcessorService"

        const val ACTION_PROCESS_URL =
            "com.justsoft.redditshareinterceptor.action.PROCESS_REDDIT_URL"
    }
}
package com.justsoft.redditshareinterceptor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.content.FileProvider
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.util.VolleyRequestHelper
import java.io.File
import java.io.OutputStream
import java.util.concurrent.Executors

class UniversalProcessorForegroundService: Service() {

    private val mHandler = Handler(Looper.getMainLooper())
    private val mBackgroundExecutor = Executors.newSingleThreadExecutor { Thread("UUFSBackgroundThread") }

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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // END LIFECYCLE METHODS

    // PROCESSOR HANDLER EVENTS

    private fun onError(err: Throwable) {
        Log.e(LOG_TAG, "Error processing post", err)
        FirebaseCrashlytics.getInstance().recordException(err)

        mHandler.post {
            Toast.makeText(
                applicationContext,
                "Error processing post: ${err.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onResult(processingResult: ProcessingResult) {
        mHandler.post {
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
        }
    }

    // END PROCESSOR HANDLER EVENTS

    // UTILITY METHODS

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

        fun enqueueWork(context: Context, work: Intent) {
            Log.d(LOG_TAG, "enqueueWork(context, work)")
            JobIntentService.enqueueWork(context, RedditProcessorService::class.java, 1000, work)
        }
    }
}
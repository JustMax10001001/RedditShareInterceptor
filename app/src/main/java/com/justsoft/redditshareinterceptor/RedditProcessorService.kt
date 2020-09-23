package com.justsoft.redditshareinterceptor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.content.FileProvider
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.VolleyRequestHelper
import java.io.File
import java.io.OutputStream

const val ACTION_PROCESS_REDDIT_URL =
    "com.justsoft.redditshareinterceptor.action.PROCESS_REDDIT_URL"

class RedditProcessorService : JobIntentService() {

    private val mHandler = Handler(Looper.getMainLooper())

    private val mUniversalUrlProcessor: UniversalUrlProcessor by lazy {
        UniversalUrlProcessor(
            VolleyRequestHelper(Volley.newRequestQueue(applicationContext)),
            this::getUriForContentType,
            this::openStreamForUri
        )
    }

    override fun onHandleWork(intent: Intent) {
        Log.d(LOG_TAG, "Caught intent!")

        when (intent.action) {
            ACTION_PROCESS_REDDIT_URL -> {
                val url = intent.extras?.get(Intent.EXTRA_TEXT).toString()
                FirebaseCrashlytics.getInstance().setCustomKey("url", url)
                mUniversalUrlProcessor.handleUrl(url)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate()")
        FirebaseAnalyticsHelper.getInstance(this)
        mUniversalUrlProcessor.error {
            Log.e(LOG_TAG, "Error processing post", it)
            FirebaseCrashlytics.getInstance().recordException(it)

            mHandler.post {
                Toast.makeText(
                    applicationContext,
                    "Error processing post: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        mUniversalUrlProcessor.result { processingResult ->
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
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy()")
    }

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

    // UTILITY METHODS

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
        const val LOG_TAG = "ProcessorService"

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
            enqueueWork(context, RedditProcessorService::class.java, 1000, work)
        }
    }
}

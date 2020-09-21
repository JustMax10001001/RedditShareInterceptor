package com.justsoft.redditshareinterceptor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.content.FileProvider
import com.android.volley.toolbox.Volley
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.VolleyRequestHelper
import java.io.File

const val ACTION_PROCESS_REDDIT_URL =
    "com.justsoft.redditshareinterceptor.action.PROCESS_REDDIT_URL"

class RedditProcessorService : JobIntentService() {

    private val mHandler = Handler(Looper.getMainLooper())

    private val mRedditPostHandler: RedditPostHandler by lazy {
        RedditPostHandler(VolleyRequestHelper(Volley.newRequestQueue(applicationContext)))
    }

    override fun onHandleWork(intent: Intent) {
        Log.d(LOG_TAG, "Caught intent!")

        when (intent.action) {
            ACTION_PROCESS_REDDIT_URL -> {
                val url = intent.extras?.get(Intent.EXTRA_TEXT).toString()
                FirebaseCrashlytics.getInstance().setCustomKey("url", url)
                mRedditPostHandler.handlePostUrl(url, this::createFileDescriptor)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate()")
        mRedditPostHandler.error {
            Log.e(LOG_TAG, "Error processing post", it)
            mHandler.post {
                Toast.makeText(
                    applicationContext,
                    "Error processing reddit post: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        mRedditPostHandler.mediaSuccess { contentType, redditPost, imageCount ->
            mHandler.post {
                startActivity(
                    when (contentType) {
                        ContentType.GALLERY -> prepareMediaMultipleIntent(
                            contentType,
                            redditPost,
                            imageCount
                        )
                        else -> prepareMediaIntent(contentType, redditPost)
                    }
                )
            }
        }
        mRedditPostHandler.textSuccess { redditPost ->
            mHandler.post { startActivity(prepareTextIntent(redditPost)) }
        }
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "onDestroy()")
    }

    // INTENT CREATION

    private fun prepareMediaMultipleIntent(
        contentType: ContentType,
        redditPost: RedditPost,
        imageCount: Int
    ): Intent {
        return prepareIntent(
            getMimeForContentType(contentType),
            "${redditPost.subreddit}\r\n${redditPost.title}"
        ).apply {
            action = Intent.ACTION_SEND_MULTIPLE

            val uriList = ArrayList<Uri>()
            for (i in 0 until imageCount)
                uriList.add(getInternalFileUri(getFileNameForContentType(contentType, i)))

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        }
    }

    private fun prepareTextIntent(redditPost: RedditPost): Intent =
        prepareIntent(
            "text/*",
            redditPost.subreddit +
                    "\r\n${redditPost.title}" +
                    "\r\n${redditPost.selftext}"
        )

    private fun prepareMediaIntent(contentType: ContentType, redditPost: RedditPost): Intent =
        prepareIntent(
            getMimeForContentType(contentType),
            "${redditPost.subreddit}\r\n${redditPost.title}"
        ).putExtra(Intent.EXTRA_STREAM, getInternalFileUri(getFileNameForContentType(contentType)))

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

    private fun createFileDescriptor(
        contentType: ContentType,
        mediaIndex: Int
    ): ParcelFileDescriptor =
        contentResolver.openFileDescriptor(
            getInternalFileUri(getFileNameForContentType(contentType, mediaIndex)),
            "w"
        )!!

    private fun getInternalFileUri(file: String): Uri {
        return FileProvider.getUriForFile(
            this,
            "com.justsoft.redditshareinterceptor.provider",
            File(filesDir, file)
        )
    }

    private fun getFileNameForContentType(contentType: ContentType, index: Int = 0): String =
        (contentTypeToFileNameMap[contentType]
            ?: error("No such key: $contentType in Filename map")).format(index)

    private fun getMimeForContentType(contentType: ContentType): String =
        contentTypeToMIME[contentType] ?: error("No such key: $contentType in MIME map")

    // END UTILITY METHODS

    companion object {
        const val LOG_TAG = "ProcessorService"

        private val contentTypeToFileNameMap = mapOf(
            ContentType.GIF to "gif.mp4",
            ContentType.VIDEO to "video.mp4",
            ContentType.IMAGE to "image.jpg",
            ContentType.GALLERY to "image_%d.jpg",
        )

        private val contentTypeToMIME = mapOf(
            ContentType.GIF to "video/*",
            ContentType.VIDEO to "video/*",
            ContentType.IMAGE to "image/*",
            ContentType.GALLERY to "image/*",
        )

        fun enqueueWork(context: Context, work: Intent) {
            Log.d(LOG_TAG, "enqueueWork(context, work)")
            enqueueWork(context, RedditProcessorService::class.java, 1000, work)
        }
    }
}

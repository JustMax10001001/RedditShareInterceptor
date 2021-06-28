package com.justsoft.redditshareinterceptor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.justsoft.redditshareinterceptor.components.services.UniversalProcessorForegroundService

class SendNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: throw IllegalArgumentException("Context can not be null")
        intent ?: throw IllegalArgumentException("Intent can not be null")

        // cancel notification
        val mNotificationManager: NotificationManagerCompat =
            NotificationManagerCompat.from(context)
        mNotificationManager.cancel(UniversalProcessorForegroundService.DOWNLOAD_FINISHED_NOTIFICATION_ID)
        Log.d(LOG_TAG, "Cancelled notification")

        if (intent.action != ACTION_SHARE_MEDIA)
            throw IllegalArgumentException("Unknown action: ${intent.action}")

        val mediaType = intent.getIntExtra(KEY_MEDIA_FLAG, -1)
        val caption = intent.getStringExtra(KEY_MEDIA_CAPTION) ?: ""
        val mimeType = intent.getStringExtra(KEY_MIME_TYPE) ?: "text/plain"
        val sendIntent = when (mediaType) {
            FLAG_NO_MEDIA -> prepareTextIntent(caption, mimeType)
            FLAG_SINGLE_MEDIA -> prepareMediaIntent(
                caption,
                intent.getParcelableExtra(KEY_MEDIA_SINGLE_URI)
                    ?: throw IllegalArgumentException("No key $KEY_MEDIA_SINGLE_URI in intent!"),
                mimeType
            )
            FLAG_MULTIPLE_MEDIA -> prepareMediaMultipleIntent(
                caption,
                intent.getParcelableArrayListExtra(KEY_MEDIA_URI_LIST)
                    ?: throw IllegalArgumentException("No key $KEY_MEDIA_URI_LIST in intent!"),
                mimeType
            )
            else -> throw IllegalArgumentException("Unknown media flag: $mediaType")
        }
        context.startActivity(sendIntent)
    }

    private fun prepareMediaMultipleIntent(
        caption: String,
        uris: List<Uri>,
        mimeType: String
    ): Intent {
        return prepareIntent(mimeType, caption).apply {
            action = Intent.ACTION_SEND_MULTIPLE

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
    }

    private fun prepareTextIntent(caption: String, mimeType: String): Intent =
        prepareIntent(mimeType, caption)

    private fun prepareMediaIntent(
        caption: String,
        mediaUri: Uri,
        mimeType: String
    ): Intent =
        prepareIntent(
            mimeType, caption
        ).putExtra(Intent.EXTRA_STREAM, mediaUri)

    private fun prepareIntent(mimeType: String, extraText: String): Intent =
        Intent().apply {
            action = Intent.ACTION_SEND
            type = mimeType

            putExtra(Intent.EXTRA_TEXT, extraText)

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    companion object {
        const val FLAG_NO_MEDIA = 10
        const val FLAG_SINGLE_MEDIA = 11
        const val FLAG_MULTIPLE_MEDIA = 12

        const val KEY_MEDIA_CAPTION = "media_caption"
        const val KEY_MEDIA_SINGLE_URI = "media_single_uri"
        const val KEY_MEDIA_URI_LIST = "media_uri_list"
        const val KEY_MEDIA_FLAG = "media_flag"
        const val KEY_MIME_TYPE = "media_mime_type"

        const val ACTION_SHARE_MEDIA = "share_media"

        private const val LOG_TAG = "SendMediaBroadcast"
    }
}
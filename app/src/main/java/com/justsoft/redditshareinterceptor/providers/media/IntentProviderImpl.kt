package com.justsoft.redditshareinterceptor.providers.media

import android.content.Context
import android.content.Intent
import com.justsoft.redditshareinterceptor.components.broadcast_receivers.NotificationBroadcastReceiver
import com.justsoft.redditshareinterceptor.model.MediaPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IntentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IntentProvider {

    override fun provideInternalMediaIntent(mediaPost: MediaPost) =
        Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = ACTION_SHARE_MEDIA
            putExtra(KEY_MEDIA_POST, mediaPost)
        }

    override fun parseInternalMediaIntent(intent: Intent): MediaPost {
        if (intent.action != ACTION_SHARE_MEDIA) error("Unexpected action ${intent.action}")

        return intent.getParcelableExtra(KEY_MEDIA_POST)
            ?: error("No media post supplied!")
    }

    override fun provideMediaShareIntent(mediaPost: MediaPost) = Intent().apply {
        action = getAction(mediaPost)
        type = getMimeType(mediaPost)

        putExtra(Intent.EXTRA_TEXT, buildPostContent(mediaPost))
        addAttachmentsFrom(mediaPost)

        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun getAction(mediaPost: MediaPost) = when (mediaPost.attachmentType) {
        MediaContentType.GALLERY -> Intent.ACTION_SEND_MULTIPLE
        else -> Intent.ACTION_SEND
    }

    private fun getMimeType(mediaPost: MediaPost) = contentTypeToMime[mediaPost.attachmentType]

    private fun buildPostContent(mediaPost: MediaPost): String = buildString {
        fun appendIfNotEmpty(text: String) {
            if (text.isNotEmpty()) appendLine(text)
        }

        appendIfNotEmpty(mediaPost.community)
        appendIfNotEmpty(mediaPost.title)
        appendIfNotEmpty(mediaPost.bodyText)
    }

    private fun Intent.addAttachmentsFrom(mediaPost: MediaPost) {
        when (mediaPost.attachmentType) {
            MediaContentType.GALLERY -> this.putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                ArrayList(mediaPost.gallery.map { it.mediaUri })
            )
            MediaContentType.TEXT -> {
            }
            else -> this.putExtra(Intent.EXTRA_STREAM, mediaPost.attachment)
        }
    }

    companion object {
        private val contentTypeToMime = mapOf(
            MediaContentType.GIF to "video/*",
            MediaContentType.VIDEO to "video/*",
            MediaContentType.IMAGE to "image/*",
            MediaContentType.GALLERY to "image/*",
            MediaContentType.TEXT to "text/plain",
            MediaContentType.AUDIO to "audio/mp4",
            MediaContentType.VIDEO_AUDIO to "video/*",
        )

        private const val KEY_MEDIA_POST = "media_post"
        private const val ACTION_SHARE_MEDIA = "share_media"
    }
}
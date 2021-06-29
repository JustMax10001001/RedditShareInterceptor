package com.justsoft.redditshareinterceptor.model

import android.os.Parcelable
import com.justsoft.redditshareinterceptor.model.media.MediaAttachment
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaPost(
    val attachmentType: MediaContentType,
    val community: String = "",
    val title: String = "",
    val bodyText: String = "",
    private val innerItems: List<MediaAttachment> = emptyList()
) : Parcelable {
    val gallery: List<MediaAttachment>
        get() {
            if (attachmentType != MediaContentType.GALLERY) error("This is not a gallery")
            return innerItems
        }

    val attachment: MediaAttachment
        get() {
            if (attachmentType == MediaContentType.GALLERY || attachmentType == MediaContentType.TEXT)
                error("This post has a gallery or does not contain attachments")

            return innerItems.single()
        }
}
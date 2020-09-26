package com.justsoft.redditshareinterceptor.model.media

import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata

data class MediaDownloadObject(
    val downloadUrl: String,
    val mediaType: MediaContentType,
    val galleryIndex: Int = 0,
    val metadata: MediaMetadata = MediaMetadata()
)
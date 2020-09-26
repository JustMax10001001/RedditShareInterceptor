package com.justsoft.redditshareinterceptor.model.media

import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata

data class MediaDownloadObject(
    val downloadUrl: String,
    val mediaType: MediaContentType,
    val size: Long = 0,
    val galleryIndex: Int = 0,         // used for galleries
    val metadata: MediaMetadata = MediaMetadata()
)
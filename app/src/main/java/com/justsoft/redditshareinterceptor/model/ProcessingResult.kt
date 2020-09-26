package com.justsoft.redditshareinterceptor.model

import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject

class ProcessingResult(
    val contentType: MediaContentType,
    val caption: String,
    val downloadedMedia: List<MediaDownloadObject> = listOf(),
    var processingTime: Long = 0
)

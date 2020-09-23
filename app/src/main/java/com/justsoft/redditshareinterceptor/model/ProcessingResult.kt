package com.justsoft.redditshareinterceptor.model

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.media.MediaContentType

class ProcessingResult(
    val contentType: MediaContentType,
    val caption: String,
    val mediaUris: List<Uri> = listOf()
)

package com.justsoft.redditshareinterceptor.model.media

data class MediaModel(
    val downloadUrl: String,
    val size: Long = 0,
    val mediaType: MediaContentType,
    val index: Int = 0,         // used for galleries
)
package com.justsoft.redditshareinterceptor.model.media

data class MediaModel(
    val downloadUrl: String,
    val mediaType: MediaContentType,
    val size: Long = 0,
    val index: Int = 0,         // used for galleries
    val caption: String = "",  // used for text posts
)
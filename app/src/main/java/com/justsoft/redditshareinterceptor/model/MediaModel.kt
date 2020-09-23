package com.justsoft.redditshareinterceptor.model

data class MediaModel(
    val downloadUrl: String,
    val size: Long = 0,
    val mediaType: ContentType
)
package com.justsoft.redditshareinterceptor.model

class ProcessingResult private constructor(
    val processingTime: Long,
    val processingSuccessful: Boolean
) {

    lateinit var cause: Throwable
        private set

    lateinit var mediaPost: MediaPost
        private set

    companion object {
        fun error(cause: Throwable, processingTime: Long): ProcessingResult =
            ProcessingResult(processingTime, false).apply {
                this.cause = cause
            }

        fun success(mediaPost: MediaPost, processingTime: Long): ProcessingResult =
            ProcessingResult(processingTime, true).apply {
                this.mediaPost = mediaPost
            }
    }
}

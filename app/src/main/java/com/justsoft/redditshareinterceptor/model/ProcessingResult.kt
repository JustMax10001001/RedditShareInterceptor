package com.justsoft.redditshareinterceptor.model

import com.justsoft.redditshareinterceptor.model.media.MediaDownloadInfo

class ProcessingResult private constructor(
    val processingTime: Long,
    val processingSuccessful: Boolean
) {

    lateinit var cause: Throwable
        private set

    lateinit var mediaInfo: MediaDownloadInfo
        private set

    companion object {
        fun error(cause: Throwable, processingTime: Long): ProcessingResult =
            ProcessingResult(processingTime, false).apply {
                this.cause = cause
            }

        fun success(mediaInfo: MediaDownloadInfo, processingTime: Long): ProcessingResult =
            ProcessingResult(processingTime, true).apply {
                this.mediaInfo = mediaInfo
            }
    }
}

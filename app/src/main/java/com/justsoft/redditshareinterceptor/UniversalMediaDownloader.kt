package com.justsoft.redditshareinterceptor

import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadInfo
import com.justsoft.redditshareinterceptor.utils.downloaders.SingleFileDownloader
import javax.inject.Inject

class UniversalMediaDownloader @Inject constructor(
    private val singleFileDownloader: SingleFileDownloader
) {

    //private val multipleFileDownloader = MultipleFileDownloader(requestHelper, outputStreamCallback)
    //private val singleFileDownloader = SingleFileDownloader(requestHelper, outputStreamCallback)

    fun downloadMediaList(
        mediaInfo: MediaDownloadInfo,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        if (mediaInfo.mediaDownloadList.count() > 1) {
            TODO("Use MultipleFileDownloader")
            //multipleFileDownloader.downloadFiles(mediaInfo.mediaDownloadList, downloadProgressCallback)
        } else {
            singleFileDownloader.downloadFile(
                mediaInfo.mediaDownloadList.single(),
                downloadProgressCallback
            )
        }
    }
}
package com.justsoft.redditshareinterceptor

import android.net.Uri
import com.justsoft.redditshareinterceptor.downloaders.MultipleFileDownloader
import com.justsoft.redditshareinterceptor.downloaders.SingleFileDownloader
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadInfo
import com.justsoft.redditshareinterceptor.util.request.RequestHelper
import java.io.OutputStream

class UniversalMediaDownloader(
    requestHelper: RequestHelper,
    outputStreamCallback: (Uri) -> OutputStream
) {

    private val multipleFileDownloader = MultipleFileDownloader(requestHelper, outputStreamCallback)
    private val singleFileDownloader = SingleFileDownloader(requestHelper, outputStreamCallback)

    fun downloadMediaList(
        mediaInfo: MediaDownloadInfo,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        if (mediaInfo.mediaContentType == MediaContentType.GALLERY) {
            multipleFileDownloader.downloadFiles(mediaInfo.mediaDownloadList, downloadProgressCallback)
        } else {
            singleFileDownloader.downloadFile(mediaInfo.mediaDownloadList.first(), downloadProgressCallback)
        }
    }
}
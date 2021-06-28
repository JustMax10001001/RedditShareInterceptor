package com.justsoft.redditshareinterceptor

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadInfo
import com.justsoft.redditshareinterceptor.utils.downloaders.MultipleFileDownloader
import com.justsoft.redditshareinterceptor.utils.downloaders.SingleFileDownloader
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
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
        if (mediaInfo.mediaDownloadList.count() > 1) {
            multipleFileDownloader.downloadFiles(mediaInfo.mediaDownloadList, downloadProgressCallback)
        } else {
            singleFileDownloader.downloadFile(mediaInfo.mediaDownloadList.single(), downloadProgressCallback)
        }
    }
}
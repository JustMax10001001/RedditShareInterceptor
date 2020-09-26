package com.justsoft.redditshareinterceptor

import android.net.Uri
import com.justsoft.redditshareinterceptor.downloaders.MultipleFileDownloader
import com.justsoft.redditshareinterceptor.downloaders.SingleFileDownloader
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.io.OutputStream

class UniversalMediaDownloader(
    requestHelper: RequestHelper,
    outputStreamCallback: (Uri) -> OutputStream
) {

    private val multipleFileDownloader = MultipleFileDownloader(requestHelper, outputStreamCallback)
    private val singleFileDownloader = SingleFileDownloader(requestHelper, outputStreamCallback)

    fun downloadMediaList(
        mediaList: MediaDownloadList,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        if (mediaList.listMediaContentType == MediaContentType.GALLERY) {
            multipleFileDownloader.downloadFiles(mediaList, downloadProgressCallback)
        } else {
            singleFileDownloader.downloadFile(mediaList.first(), downloadProgressCallback)
        }
    }
}
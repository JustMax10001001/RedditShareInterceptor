package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.io.OutputStream

interface MediaDownloader {

    /**
     * @return list of downloaded file URIs
     */
    fun downloadMedia(
        mediaList: MediaDownloadList,
        requestHelper: RequestHelper,
        destinationUriCallback: (MediaContentType, Int) -> Uri,
        outputStreamCallback: (Uri) -> OutputStream,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ): List<Uri>
}
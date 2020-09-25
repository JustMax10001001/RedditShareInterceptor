package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.io.OutputStream

class GifDownloader : MediaDownloader {
    override fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        destinationUriCallback: (MediaContentType, Int) -> Uri,
        outputStreamCallback: (Uri) -> OutputStream,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ): List<Uri> {
        val uri = destinationUriCallback(MediaContentType.VIDEO, 0)
        var prevProgress = 0
        requestHelper.downloadToOutputStream(
            mediaList[0].downloadUrl,
            outputStreamCallback(uri)
        ) { processingProgress ->
            val progress =
                (processingProgress.overallProgress.toDouble() / mediaList[0].size * 100).toInt()
            if (progress != prevProgress) {
                prevProgress = progress
                downloadProgressCallback(ProcessingProgress(-1, progress))
            }
        }
        return listOf(uri)
    }
}
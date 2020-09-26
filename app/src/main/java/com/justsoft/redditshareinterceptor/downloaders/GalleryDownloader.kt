package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.OutputStream

class GalleryDownloader : MediaDownloader {
    override fun downloadMedia(
        mediaList: MediaDownloadList,
        requestHelper: RequestHelper,
        destinationUriCallback: (MediaContentType, Int) -> Uri,
        outputStreamCallback: (Uri) -> OutputStream,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ): List<Uri> {
        val uris = mutableListOf<Uri>()
        runBlocking(Dispatchers.IO) {
            val imageCount = mediaList.count()
            var prevProgress = 0
            var totalProgress = 0
            for (i in 0 until imageCount) {
                launch {
                    val uri = destinationUriCallback(MediaContentType.GALLERY, i)
                    requestHelper.downloadToOutputStream(
                        mediaList[i].downloadUrl,
                        outputStreamCallback(uri)
                    ) { processingProgress ->
                        // requestHelper returns the total amount of bytes read
                        totalProgress += (processingProgress.overallProgress / mediaList[i].size.toDouble() * 100 / imageCount).toInt()
                        if (totalProgress != prevProgress) {
                            downloadProgressCallback(
                                ProcessingProgress(-1, totalProgress)
                            )
                            prevProgress = totalProgress
                        }
                    }
                    uris.add(uri)
                }
            }
        }
        return uris
    }
}
package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.io.OutputStream

class GifDownloader : MediaDownloader {
    override fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        destinationUriCallback: (MediaContentType, Int) -> Uri,
        outputStreamCallback: (Uri) -> OutputStream
    ): List<Uri> {
        val uri = destinationUriCallback(MediaContentType.VIDEO, 0)
        requestHelper.downloadToOutputStream(
            mediaList[0].downloadUrl,
            outputStreamCallback(uri)
        )
        return listOf(uri)
    }
}
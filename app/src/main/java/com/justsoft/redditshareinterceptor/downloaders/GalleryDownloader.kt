package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.OutputStream

class GalleryDownloader: MediaDownloader {
    override fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        destinationUriCallback: (MediaContentType, Int) -> Uri,
        outputStreamCallback: (Uri) -> OutputStream
    ): List<Uri> {
        val uris = mutableListOf<Uri>()
        runBlocking(Dispatchers.IO) {
            for (i in 0 until mediaList.count()) {
                val uri = destinationUriCallback(MediaContentType.VIDEO, 0)
                requestHelper.downloadToOutputStream(
                    mediaList[0].downloadUrl,
                    outputStreamCallback(uri)
                )
                uris.add(uri)
            }
        }
        return uris
    }
}
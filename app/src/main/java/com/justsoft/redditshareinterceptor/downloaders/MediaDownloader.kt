package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.io.OutputStream

interface MediaDownloader {

    /**
     * @return list of downloaded file URIs
     */
    fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        destinationUriCallback: (MediaContentType, Int) -> Uri,
        outputStreamCallback: (Uri) -> OutputStream
    ): List<Uri>
}
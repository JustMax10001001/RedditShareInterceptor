package com.justsoft.redditshareinterceptor.downloaders

import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper

class GifDownloader : MediaDownloader {
    override fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        createDestinationFileDescriptor: (MediaContentType, Int) -> ParcelFileDescriptor
    ): Int {
        requestHelper.downloadFile(
            mediaList[0].downloadUrl,
            createDestinationFileDescriptor(MediaContentType.GIF, 0)
        )
        return 1
    }
}
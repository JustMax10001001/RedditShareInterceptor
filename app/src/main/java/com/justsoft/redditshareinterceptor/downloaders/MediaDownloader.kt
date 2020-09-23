package com.justsoft.redditshareinterceptor.downloaders

import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper

interface MediaDownloader {

    fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        createDestinationFileDescriptor: (MediaContentType, Int) -> ParcelFileDescriptor
    ): Int
}
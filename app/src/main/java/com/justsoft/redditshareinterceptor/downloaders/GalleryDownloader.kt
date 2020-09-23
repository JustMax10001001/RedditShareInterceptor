package com.justsoft.redditshareinterceptor.downloaders

import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class GalleryDownloader: MediaDownloader {
    override fun downloadMedia(
        mediaList: MediaList,
        requestHelper: RequestHelper,
        createDestinationFileDescriptor: (MediaContentType, Int) -> ParcelFileDescriptor
    ): Int {
        runBlocking(Dispatchers.IO) {
            for (i in 0 until mediaList.count()) {
                requestHelper.downloadFile(
                    mediaList[i].downloadUrl,
                    createDestinationFileDescriptor(MediaContentType.GALLERY, i)
                )
            }
        }
        return mediaList.count()
    }
}
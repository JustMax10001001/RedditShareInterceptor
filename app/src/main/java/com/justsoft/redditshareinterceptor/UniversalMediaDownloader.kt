package com.justsoft.redditshareinterceptor

import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.downloaders.*
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper

class UniversalMediaDownloader(
    private val requestHelper: RequestHelper,
    private val destinationDescriptorGenerator: (MediaContentType, Int) -> ParcelFileDescriptor
) {

    fun downloadMediaList(mediaList: MediaList): Int {
        return selectDownloaderForMediaType(mediaList.listMediaContentType)
                 .downloadMedia(mediaList, requestHelper, destinationDescriptorGenerator)
    }

    private fun selectDownloaderForMediaType(mediaContentType: MediaContentType): MediaDownloader =
        downloaders[mediaContentType] ?: error("No downloader for type $mediaContentType")

    private val downloaders: Map<MediaContentType, MediaDownloader> = mapOf(
        MediaContentType.GIF to GifDownloader(),
        MediaContentType.VIDEO to VideoDownloader(),
        MediaContentType.IMAGE to ImageDownloader(),
        MediaContentType.GALLERY to GalleryDownloader()
    )
}
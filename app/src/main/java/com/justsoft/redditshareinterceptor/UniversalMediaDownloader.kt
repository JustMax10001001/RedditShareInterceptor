package com.justsoft.redditshareinterceptor

import android.net.Uri
import com.justsoft.redditshareinterceptor.downloaders.*
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.io.OutputStream

class UniversalMediaDownloader(
    private val requestHelper: RequestHelper,
    private val destinationUriCallback: (MediaContentType, Int) -> Uri,
    private val outputStreamCallback: (Uri) -> OutputStream
) {

    fun downloadMediaList(
        mediaList: MediaList,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ): List<Uri> {
        return selectDownloaderForMediaType(mediaList.listMediaContentType)
            .downloadMedia(
                mediaList,
                requestHelper,
                destinationUriCallback,
                outputStreamCallback,
                downloadProgressCallback
            )
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
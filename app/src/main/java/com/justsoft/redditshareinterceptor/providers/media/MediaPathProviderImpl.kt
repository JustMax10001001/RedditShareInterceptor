package com.justsoft.redditshareinterceptor.providers.media

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.*
import com.justsoft.redditshareinterceptor.providers.io.InternalPathProvider
import com.justsoft.redditshareinterceptor.providers.io.InternalUriProvider
import java.io.File
import javax.inject.Inject

class MediaPathProviderImpl @Inject constructor(
    private val pathProvider: InternalPathProvider,
    private val uriProvider: InternalUriProvider
) : MediaPathProvider {
    override fun getUriForMediaType(mediaType: MediaContentType, galleryIndex: Int): Uri {
        val fileName = getFileName(mediaType, galleryIndex)
        val path = pathProvider.getInternalPath(fileName)

        return uriProvider.provideUriForPath(path)
    }

    override fun getFileForMediaType(mediaType: MediaContentType, galleryIndex: Int): File {
        val fileName = getFileName(mediaType, galleryIndex)
        return pathProvider.getInternalFile(fileName)
    }

    private fun getFileName(mediaType: MediaContentType, galleryIndex: Int): String {
        return when {
            mediaType == GALLERY -> contentTypeToFileNameMap[GALLERY]!!.format(galleryIndex)
            contentTypeToFileNameMap.containsKey(mediaType) -> contentTypeToFileNameMap[mediaType]!!
            else -> throw IllegalArgumentException("No file name mapping for content type $mediaType")
        }
    }

    companion object {
        private val contentTypeToFileNameMap = mapOf(
            GIF to "gif.mp4",
            VIDEO to "video.mp4",
            IMAGE to "image.jpg",
            GALLERY to "image_%d.jpg",
            AUDIO to "audio.mp4",
            VIDEO_AUDIO to "video_w_audio.mp4"
        )
    }
}
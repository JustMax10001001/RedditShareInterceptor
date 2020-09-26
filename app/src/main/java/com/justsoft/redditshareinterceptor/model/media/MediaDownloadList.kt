package com.justsoft.redditshareinterceptor.model.media

import com.justsoft.redditshareinterceptor.model.media.MediaContentType.GIF
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.VIDEO
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata.Companion.KEY_BITRATE
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata.Companion.KEY_RESOLUTION_X
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata.Companion.KEY_RESOLUTION_Y
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata.Companion.KEY_SIZE_BYTES
import kotlin.math.abs

class MediaDownloadList(val listMediaContentType: MediaContentType, var caption: String = "") :
    ArrayList<MediaDownloadObject>() {

    /*private fun processSubList(mediaSpec: MediaQualitySpec, sortedList: List<MediaDownloadObject>, isVideo: Boolean): MediaDownloadObject {
        val threshold = mediaSpec.getThresholdForType(listMediaContentType)
        val iterator = sortedList.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.size <= threshold)
                return file
        }
        return sortedList.last()
    }

    fun getMostSuitableMedia(mediaSpec: MediaQualitySpec): MediaDownloadList {
        if (this.isEmpty())
            throw IllegalStateException("MediaList is empty!")

        if (this.count() == 1)
            return mediaDownloadListOf(this)



        val sortedList = this.sortedWith(compareBy(MediaDownloadObject::galleryIndex, { -it.size }))
        return MediaDownloadList(listMediaContentType, caption).apply {
            addAll(
                sortedList
                    .groupBy(MediaDownloadObject::galleryIndex)
                    .map { processSubList(mediaSpec, it.value) }
            )
        }
    }*/
    fun getMostSuitableMedia(mediaQualitySpec: MediaQualitySpec): MediaDownloadList {
        if (this.isEmpty())
            throw IllegalStateException("MediaList is empty!")
        if (this.count() == 1)
            return mediaDownloadListOf(this)

        val isVideo = listMediaContentType == VIDEO || listMediaContentType == GIF
        val list = MediaDownloadList(listMediaContentType, caption)
        list.addAll(
            this.groupBy(MediaDownloadObject::galleryIndex)
                .map { processSublist(mediaQualitySpec, it.value, isVideo) }
        )
        return list
    }

    private fun processSublist(
        mediaQualitySpec: MediaQualitySpec,
        sublist: List<MediaDownloadObject>,
        isVideo: Boolean
    ): MediaDownloadObject {
        val meta = sublist.first().metadata         // assume that all entries have similar metadata
        return if (isVideo) {
            when {
                meta.hasProperty(KEY_BITRATE) -> processSublistByBitrate(mediaQualitySpec, sublist)
                meta.hasProperty(KEY_RESOLUTION_X) &&
                        meta.hasProperty(KEY_RESOLUTION_Y) -> processSublistByResolution(
                    mediaQualitySpec,
                    sublist,
                    isVideo
                )
                meta.hasProperty(KEY_SIZE_BYTES) -> processSublistByFileSize(
                    mediaQualitySpec,
                    sublist,
                    isVideo
                )
                else -> throw IllegalStateException("There are no suitable fields set in order to perform the operation")
            }
        } else {
            when {
                meta.hasProperty(KEY_RESOLUTION_X) &&
                        meta.hasProperty(KEY_RESOLUTION_Y) -> processSublistByResolution(
                    mediaQualitySpec,
                    sublist,
                    isVideo
                )
                meta.hasProperty(KEY_SIZE_BYTES) -> processSublistByFileSize(
                    mediaQualitySpec,
                    sublist,
                    isVideo
                )
                else -> throw IllegalStateException("There are no suitable fields set in order to perform the operation")
            }
        }
    }

    private fun processSublist(
        target: Long,
        sublist: List<MediaDownloadObject>,
        valueCallback: (MediaDownloadObject) -> Long
    ): MediaDownloadObject {
        val sortedSublist = sublist.sortedBy { entry -> abs(valueCallback(entry) - target) }
        return sortedSublist.first()
    }

    private fun processSublistByFileSize(
        mediaQualitySpec: MediaQualitySpec,
        sublist: List<MediaDownloadObject>,
        isVideo: Boolean
    ): MediaDownloadObject {
        return processSublist(
            (if (isVideo) mediaQualitySpec.videoFileSize else mediaQualitySpec.imageFileSize).toLong(),
            sublist
        ) {
            it.metadata.size
        }
    }

    private fun processSublistByResolution(
        mediaQualitySpec: MediaQualitySpec,
        sublist: List<MediaDownloadObject>,
        isVideo: Boolean
    ): MediaDownloadObject {
        return processSublist(
            if (isVideo)
                with(mediaQualitySpec) { videoSizeX * videoSizeY }.toLong()
            else
                with(mediaQualitySpec) { imageSizeX * imageSizeY }.toLong(),
            sublist
        ) {
            with(it.metadata) { resolutionX * resolutionY }.toLong()
        }
    }

    private fun processSublistByBitrate(
        mediaQualitySpec: MediaQualitySpec,
        sublist: List<MediaDownloadObject>
    ): MediaDownloadObject {
        return processSublist(mediaQualitySpec.videoBitrate.toLong(), sublist) { it.metadata.bitrate.toLong() }
    }
}

fun mediaDownloadListOf(mediaType: MediaContentType, caption: String = ""): MediaDownloadList =
    MediaDownloadList(mediaType, caption)

fun mediaDownloadListOf(contentType: MediaContentType): MediaDownloadList =
    mediaDownloadListOf(contentType, "")

fun mediaDownloadListOf(caption: String, vararg media: MediaDownloadObject): MediaDownloadList =
    mediaDownloadListOf(media[0].mediaType, caption).apply { this.addAll(media) }

fun mediaDownloadListOf(mediaList: MediaDownloadList): MediaDownloadList =
    MediaDownloadList(mediaList.listMediaContentType, mediaList.caption).apply {
        this.addAll(
            mediaList
        )
    }

fun mediaDownloadListOf(vararg media: MediaDownloadObject): MediaDownloadList =
    mediaDownloadListOf(media[0].mediaType).apply { this.addAll(media) }
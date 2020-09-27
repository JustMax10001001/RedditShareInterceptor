package com.justsoft.redditshareinterceptor.model.media

import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata
import kotlin.math.abs

class MediaListFilter {

    companion object {

        fun filterListByQualitySpec(
            mediaQualitySpec: MediaQualitySpec,
            srcMediaList: List<MediaDownloadObject>,
            contentType: MediaContentType
        ): List<MediaDownloadObject> {
            if (srcMediaList.isEmpty())
                throw IllegalStateException("MediaList is empty!")
            if (srcMediaList.count() == 1)
                return listOf(srcMediaList.first())

            val isVideo =
                contentType == MediaContentType.VIDEO || contentType == MediaContentType.GIF
            val list = mutableListOf<MediaDownloadObject>()
            list.addAll(
                srcMediaList.groupBy(MediaDownloadObject::galleryIndex)
                    .map { processSublist(mediaQualitySpec, it.value, isVideo) }
            )
            return list
        }

        private fun processSublist(
            mediaQualitySpec: MediaQualitySpec,
            sublist: List<MediaDownloadObject>,
            isVideo: Boolean
        ): MediaDownloadObject {
            val meta =
                sublist.first().metadata         // assume that all entries have similar metadata
            return if (isVideo) {
                when {
                    meta.hasProperty(MediaMetadata.KEY_BITRATE) -> processSublistByBitrate(
                        mediaQualitySpec,
                        sublist
                    )
                    meta.hasProperty(MediaMetadata.KEY_RESOLUTION_X) &&
                            meta.hasProperty(MediaMetadata.KEY_RESOLUTION_Y) -> processSublistByResolution(
                        mediaQualitySpec,
                        sublist,
                        isVideo
                    )
                    meta.hasProperty(MediaMetadata.KEY_SIZE_BYTES) -> processSublistByFileSize(
                        mediaQualitySpec,
                        sublist,
                        isVideo
                    )
                    else -> throw IllegalStateException("There are no suitable fields set in order to perform the operation")
                }
            } else {
                when {
                    meta.hasProperty(MediaMetadata.KEY_RESOLUTION_X) &&
                            meta.hasProperty(MediaMetadata.KEY_RESOLUTION_Y) -> processSublistByResolution(
                        mediaQualitySpec,
                        sublist,
                        isVideo
                    )
                    meta.hasProperty(MediaMetadata.KEY_SIZE_BYTES) -> processSublistByFileSize(
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
            return processSublist(
                mediaQualitySpec.videoBitrate.toLong(),
                sublist
            ) { it.metadata.bitrate.toLong() }
        }
    }
}
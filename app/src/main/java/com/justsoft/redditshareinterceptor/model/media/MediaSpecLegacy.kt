package com.justsoft.redditshareinterceptor.model.media

class MediaSpecLegacy(
    videoSizeThreshold: Long = RECOMMENDED_VIDEO_SIZE_THRESHOLD,
    gifSizeThreshold: Long = RECOMMENDED_GIF_SIZE_THRESHOLD,
    imageSizeThreshold: Long = RECOMMENDED_IMAGE_SIZE_THRESHOLD,
    galleryImageSizeThreshold: Long = RECOMMENDED_IMAGE_SIZE_THRESHOLD
) {
    private val specMap: Map<MediaContentType, Long> = mapOf(
        MediaContentType.VIDEO to videoSizeThreshold,
        MediaContentType.GIF to gifSizeThreshold,
        MediaContentType.IMAGE to imageSizeThreshold,
        MediaContentType.GALLERY to galleryImageSizeThreshold,
    )

    fun getThresholdForType(mediaContentType: MediaContentType): Long {
        if (mediaContentType == MediaContentType.TEXT)
            throw IllegalArgumentException("There are no specs for text as it")
        return specMap[mediaContentType] ?: error("No such key: $mediaContentType")
    }

    companion object {
        private const val RECOMMENDED_VIDEO_SIZE_THRESHOLD = (10 * 1024 * 1024).toLong()    // 10 MiB
        private const val RECOMMENDED_GIF_SIZE_THRESHOLD = (10 * 1024 * 1024).toLong()      // 10 MiB
        private const val RECOMMENDED_IMAGE_SIZE_THRESHOLD = (1.5 * 1024 * 1024).toLong()   // 1.5 MiB
    }
}

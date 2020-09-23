package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper

class RedditGalleryPostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("reddit.com/gallery")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType {
        savedState.putInt(KEY_IMAGES_COUNT, redditPost.galleryImageUrls.size)
        return MediaContentType.GALLERY
    }

    override fun getAllPossibleMediaDownloads(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList {
        val imageIndex = savedState.getInt(KEY_GET_URL_OF_IMAGE_INDEX, -1)
        if (imageIndex < 0 || imageIndex >= redditPost.galleryImageUrls.size)
            throw IllegalArgumentException("savedState[$KEY_GET_URL_OF_IMAGE_INDEX] is not set, " +
                    "less then zero or bigger than number of images!")

        return redditPost.galleryImageUrls[imageIndex]
    }

    companion object {
        const val KEY_IMAGES_COUNT = "images_count"
        const val KEY_GET_URL_OF_IMAGE_INDEX = "image_index"
    }
}

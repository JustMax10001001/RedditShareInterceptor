package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.*
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class RedditGalleryPostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("reddit.com/gallery")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType =
        MediaContentType.GALLERY

    override fun downloadMediaMatchingMediaSpec(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper,
        mediaSpec: MediaSpec,
        destinationDescriptorGenerator: (MediaContentType, Int) -> ParcelFileDescriptor
    ): MediaList {
        val galleryImages = getMediaList(redditPost, requestHelper).getMostSuitableMedia(mediaSpec)
        runBlocking(Dispatchers.IO) {
            for (i in 0 until galleryImages.count()) {
                requestHelper.downloadFile(
                    galleryImages[i].downloadUrl,
                    destinationDescriptorGenerator(MediaContentType.GALLERY, i)
                )
            }
        }
        return galleryImages
    }

    private fun getMediaList(redditPost: RedditPost, requestHelper: RequestHelper): MediaList {
        return mediaListOf(MediaContentType.IMAGE).apply {
            runBlocking(Dispatchers.IO) {
                redditPost.galleryImageUrls.forEach {
                    add(MediaModel(it, requestHelper.getContentLength(it), MediaContentType.IMAGE))
                }
            }
        }
    }

    companion object {
        //const val KEY_IMAGES_COUNT = "images_count"
        //const val KEY_GET_URL_OF_IMAGE_INDEX = "image_index"
    }
}

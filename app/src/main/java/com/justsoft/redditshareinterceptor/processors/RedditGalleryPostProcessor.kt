package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaModel
import com.justsoft.redditshareinterceptor.model.media.mediaListOf
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

    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList {
        return getMediaList(redditPost, requestHelper)
    }

    private fun getMediaList(redditPost: RedditPost, requestHelper: RequestHelper): MediaList {
        return mediaListOf(MediaContentType.GALLERY).apply {
            runBlocking(Dispatchers.IO) {
                redditPost.galleryImageUrls.forEach { entry ->
                    entry.value.forEach { url ->
                        add(
                            MediaModel(
                                url,
                                MediaContentType.IMAGE,
                                requestHelper.getContentLength(url),        // get size
                                entry.key       // index
                            )
                        )
                    }
                }
            }
        }
    }
}

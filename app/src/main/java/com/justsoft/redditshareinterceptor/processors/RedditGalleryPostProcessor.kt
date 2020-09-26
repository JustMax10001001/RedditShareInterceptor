package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.mediaDownloadListOf
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
    ): MediaDownloadList {
        return getMediaList(redditPost, requestHelper)
    }

    private fun getMediaList(redditPost: RedditPost, requestHelper: RequestHelper): MediaDownloadList {
        return mediaDownloadListOf(MediaContentType.GALLERY).apply {
            runBlocking(Dispatchers.IO) {
                redditPost.galleryImageUrls.forEach { entry ->
                    entry.value.forEach { url ->
                        add(
                            MediaDownloadObject(
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

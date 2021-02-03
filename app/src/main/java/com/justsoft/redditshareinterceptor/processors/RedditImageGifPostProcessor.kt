package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata
import com.justsoft.redditshareinterceptor.util.request.RequestHelper

class RedditImageGifPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean {
        return with(redditPost.url) {
            isIReddit() && hasGifExtension()
        }
    }

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType {
        return MediaContentType.GIF
    }

    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> {
        return listOf(
            MediaDownloadObject(
                redditPost.url,
                MediaContentType.GIF,
                metadata = MediaMetadata().apply {
                    size = requestHelper.getContentLength(redditPost.url)
                }
            )
        )
    }
}
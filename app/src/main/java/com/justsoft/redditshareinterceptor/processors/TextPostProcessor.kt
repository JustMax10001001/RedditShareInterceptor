package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaModel
import com.justsoft.redditshareinterceptor.model.media.mediaListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper

class TextPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("www.reddit.com") &&
                !redditPost.url.contains("www.reddit.com/gallery")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.TEXT

    override fun getAllPossibleMediaDownloads(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList = mediaListOf(MediaContentType.TEXT, MediaModel(redditPost.url, 0, MediaContentType.TEXT))
}
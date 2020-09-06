package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.RequestHelper

class TextPostProcessor: PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("www.reddit.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): ContentType = ContentType.TEXT

    override fun getMediaDownloadUrl(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): String = redditPost.url
}
package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.*
import com.justsoft.redditshareinterceptor.util.RequestHelper

class RedditTwitterPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("twitter.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType =
        MediaContentType.TEXT

    override fun downloadMediaMatchingMediaSpec(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper,
        mediaSpec: MediaSpec,
        destinationDescriptorGenerator: (MediaContentType, Int) -> ParcelFileDescriptor
    ): MediaList = mediaListOf(
        MediaModel(
            redditPost.url,
            MediaContentType.TEXT,
            caption = "${redditPost.subreddit}\r\n${redditPost.title}\r\n${redditPost.url}"
        )
    )
}
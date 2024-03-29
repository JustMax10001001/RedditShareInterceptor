package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.util.request.RequestHelper

class RedditTextPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("www.reddit.com") &&
                !redditPost.url.isGallery()

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.TEXT

    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> = listOf(
        MediaDownloadObject(
            redditPost.url,
            MediaContentType.TEXT,
        )
    )

    override fun getPostCaption(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): String = "${redditPost.subreddit}\r\n${redditPost.title}\r\n${redditPost.selftext}"
}
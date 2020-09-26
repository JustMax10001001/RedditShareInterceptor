package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.mediaDownloadListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper

class RedditTextPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("www.reddit.com") &&
                !redditPost.url.contains("www.reddit.com/gallery")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.TEXT

    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaDownloadList = mediaDownloadListOf(
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
package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.util.RequestHelper

/**
 * An interface for processing different variations of posts
 * Provides default methods for making additional HTTP requests
 * Implementations of this interface are expected to be stateless
 */
interface PostProcessor {

    fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean

    fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType

    /**
     * @return: List of downloaded media files
     */
    fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject>

    fun getPostCaption(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): String = "${redditPost.subreddit}\r\n${redditPost.title}"
}
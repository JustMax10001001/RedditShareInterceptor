package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.RequestHelper

/**
 * An interface for processing different variations of posts
 * Provides default methods for making additional HTTP requests
 * Implementations of this interface are expected to be stateless
 */
interface PostProcessor {

    fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean

    fun getPostContentType(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): ContentType

    fun getMediaDownloadUrl(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): String
}
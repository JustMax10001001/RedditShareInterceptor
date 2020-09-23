package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.RequestHelper

class ImgurImageProcessor: PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("i.imgur.com")


    override fun getPostContentType(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): ContentType =
        ContentType.IMAGE


    override fun getMediaDownloadUrl(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): String =
        redditPost.url
}
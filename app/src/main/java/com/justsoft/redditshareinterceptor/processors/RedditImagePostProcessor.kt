package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.android.volley.RequestQueue
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.net.URL

class RedditImagePostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("i.redd.it")


    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): ContentType =
        ContentType.IMAGE


    override fun getMediaDownloadUrl(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): String =
        redditPost.url
}
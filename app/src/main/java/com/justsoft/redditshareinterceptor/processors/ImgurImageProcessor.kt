package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper

class ImgurImageProcessor: PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("i.imgur.com")


    override fun getPostContentType(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): MediaContentType =
        MediaContentType.IMAGE


    override fun getAllPossibleMediaDownloads(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): MediaList =
        redditPost.url
}
package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper
import org.jsoup.Jsoup

class RedGifsPostProcessor: PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("redgifs.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.VIDEO

    override fun getAllPossibleMediaDownloads(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList {
        val htmlDoc = Jsoup.connect(
            redditPost.url
        ).get()
        val sourceElement = htmlDoc
            .getElementsByAttributeValueMatching("src", "https://\\w*.redgifs.com/\\w*.mp4")[0]
        return sourceElement.attr("src")
    }
}
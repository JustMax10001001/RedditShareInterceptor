package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.RequestHelper
import org.jsoup.Jsoup

class GfycatPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("gfycat.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): ContentType = ContentType.VIDEO

    override fun getMediaDownloadUrl(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): String {
        val htmlDoc = Jsoup.connect(
            redditPost.url
        ).get()
        val sourceElement = htmlDoc
            .getElementsByAttributeValueMatching("src", "https://giant.gfycat.com/\\w*.mp4")[0]
        return sourceElement.attr("src")
    }
}
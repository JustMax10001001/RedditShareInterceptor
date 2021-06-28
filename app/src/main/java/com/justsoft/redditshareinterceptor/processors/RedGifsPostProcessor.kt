package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.util.stream.Collectors

class RedGifsPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("redgifs.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.VIDEO

    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> = getPossibleDownloads(redditPost, requestHelper)


    private fun getPossibleDownloads(
        redditPost: RedditPost,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> {
        val htmlDoc = Jsoup.connect(
            redditPost.url
        ).get()
        val urls = htmlDoc
            .getElementsByAttributeValueMatching("src", "https://\\w*.redgifs.com/[\\w-]*.mp4")
            .stream()
            .map { it.attr("src") }
            .collect(Collectors.toList())
        val availableDownloads = mutableListOf<MediaDownloadObject>()
        runBlocking(Dispatchers.IO) {
            urls.forEach {
                launch {
                    availableDownloads.add(
                        MediaDownloadObject(it, MediaContentType.VIDEO)
                            .apply { metadata.size = requestHelper.getContentLength(it) }
                    )
                }
            }
        }
        return availableDownloads
    }
}
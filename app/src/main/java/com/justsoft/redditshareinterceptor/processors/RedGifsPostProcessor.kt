package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaModel
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.util.stream.Collectors

class RedGifsPostProcessor: PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("redgifs.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.VIDEO

    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList {
        val videos = getPossibleDownloads(redditPost, requestHelper)
        val bestVideos = videos.getMostSuitableMedia(mediaSpec)
        requestHelper.downloadFile(
            bestVideos[0].downloadUrl,
            destinationDescriptorGenerator(MediaContentType.VIDEO, 0)
        )
        return bestVideos
    }

    private fun getPossibleDownloads(redditPost: RedditPost, requestHelper: RequestHelper): MediaList {
        val htmlDoc = Jsoup.connect(
            redditPost.url
        ).get()
        val urls = htmlDoc
            .getElementsByAttributeValueMatching("src", "https://\\w*.redgifs.com/[\\w-]*.mp4")
            .stream()
            .map { it.attr("src") }
            .collect(Collectors.toList())
        val availableDownloads = MediaList(MediaContentType.VIDEO)
        runBlocking(Dispatchers.IO) {
            urls.forEach {
                launch {
                    availableDownloads.add(
                        MediaModel(it, MediaContentType.VIDEO, requestHelper.getContentLength(it))
                    )
                }
            }
        }
        return availableDownloads
    }
}
package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.mediaDownloadListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.util.regex.Pattern

class StreamablePostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("streamable.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType =
        MediaContentType.VIDEO

    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaDownloadList {
        return getPossibleDownloads(requestHelper, redditPost)
    }

    private fun getPossibleDownloads(
        requestHelper: RequestHelper,
        redditPost: RedditPost
    ): MediaDownloadList {
        val apiResponse = requestHelper.readHttpJsonResponse(
            "$STREAMABLE_API_URL/videos/${getVideoCode(redditPost.url)}"
        )
        val filesObj = apiResponse
            .getJSONObject("files")
        val videos = mediaDownloadListOf(MediaContentType.VIDEO)
        filesObj
            .keys()
            .forEach {
                val mediaObj = filesObj.getJSONObject(it)
                videos.add(
                    if (it != "original" || apiResponse.isNull("source"))
                        MediaDownloadObject(
                            mediaObj.getString("url"),
                            MediaContentType.VIDEO,
                            mediaObj.getLong("size")
                        )
                    else
                        MediaDownloadObject(
                            apiResponse.getString("source"),
                            MediaContentType.VIDEO,
                            mediaObj.getLong("size")
                        )
                )
            }
        return videos
    }

    private fun getVideoCode(videoUrl: String): String {
        val matcher = Pattern.compile("com/([a-zA-Z0-9].+)").matcher(videoUrl)
        matcher.find()
        return matcher.group(1)!!
    }

    companion object {
        const val STREAMABLE_API_URL = "https://api.streamable.com"
    }
}
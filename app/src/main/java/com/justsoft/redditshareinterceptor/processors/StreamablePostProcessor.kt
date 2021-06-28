package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import android.util.Log
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
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

    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> {
        return getPossibleDownloads(requestHelper, redditPost)
    }

    private fun getPossibleDownloads(
        requestHelper: RequestHelper,
        redditPost: RedditPost
    ): List<MediaDownloadObject> {
        val apiResponse = requestHelper.readHttpJsonResponse(
            "$STREAMABLE_API_URL/videos/${getVideoCode(redditPost.url)}"
        )
        val filesObj = apiResponse
            .getJSONObject("files")
        val videos = mutableListOf<MediaDownloadObject>()
        filesObj
            .keys()
            .forEach {
                val mediaObj = filesObj.getJSONObject(it)
                if (mediaObj.isNull("url") && mediaObj.isNull("url")) {
                    Log.w(
                        "Streamable",
                        "Skipping media object as it does not have url"
                    )
                    return@forEach
                }
                videos.add(
                    if (it != "original" || apiResponse.isNull("source"))
                        MediaDownloadObject(
                            mediaObj.getString("url"),
                            MediaContentType.VIDEO
                        ).apply { metadata.bitrate = mediaObj.getInt("bitrate") }
                    else
                        MediaDownloadObject(
                            apiResponse.getString("source"),
                            MediaContentType.VIDEO
                        ).apply { metadata.bitrate = apiResponse.getInt("bitrate") }
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
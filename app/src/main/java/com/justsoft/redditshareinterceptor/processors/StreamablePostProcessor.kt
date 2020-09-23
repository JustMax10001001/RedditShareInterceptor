package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaModel
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

    override fun getAllPossibleMediaDownloads(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaList {
        val apiResponse = requestHelper.readHttpJsonResponse(
            "$STREAMABLE_API_URL/videos/${getVideoCode(redditPost.url)}"
        )
        val filesObj = apiResponse
            .getJSONObject("files")
        val videos = mutableListOf<MediaModel>()
        filesObj
            .keys()
            .forEach {
                val mediaObj = filesObj.getJSONObject(it)
                videos.add(
                    if (it != "original" || apiResponse.isNull("source"))
                        MediaModel(
                            mediaObj.getString("url"),
                            mediaObj.getLong("size"),
                            MediaContentType.VIDEO
                        )
                    else
                        MediaModel(
                            apiResponse.getString("source"),
                            mediaObj.getLong("size"),
                            MediaContentType.VIDEO
                        )
                )
            }
        return MediaFileChooser.getBestMediaFile(videos).downloadUrl
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
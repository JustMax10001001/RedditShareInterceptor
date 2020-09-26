package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.mediaDownloadListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.urlDecode
import org.json.JSONObject

class RedditImagePostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("i.redd.it") ||
                redditPost.url.contains("i.imgur.com")


    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType =
        MediaContentType.IMAGE


    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaDownloadList =
        getMediaList(redditPost, requestHelper)

    private fun constructImage(imageObject: JSONObject): MediaDownloadObject {
        return MediaDownloadObject(
            urlDecode(imageObject.getString("url")),
            MediaContentType.IMAGE
        ).apply {
            metadata.resolutionX = imageObject.getInt("width")
            metadata.resolutionY = imageObject.getInt("height")
        }
    }

    private fun getMediaList(redditPost: RedditPost, requestHelper: RequestHelper): MediaDownloadList {
        val image = redditPost.postData
            .getJSONObject("preview")
            .getJSONArray("images")
            .getJSONObject(0)

        val mediaDownloadList = mediaDownloadListOf(constructImage(image.getJSONObject("source")))
        val resolutions = image.getJSONArray("resolutions")
        for (i in 0 until resolutions.length()) {
            mediaDownloadList.add(constructImage(resolutions.getJSONObject(i)))
        }

        return mediaDownloadList
    }
}
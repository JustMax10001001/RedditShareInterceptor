package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
import com.justsoft.redditshareinterceptor.utils.urlDecode
import org.json.JSONObject

class RedditImagePostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        with(redditPost.url) { (isIReddit() || isIImgur()) && !hasGifExtension()}

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType =
        MediaContentType.IMAGE


    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> =
        getMediaList(redditPost)

    private fun constructImage(imageObject: JSONObject): MediaDownloadObject {
        return MediaDownloadObject(
            urlDecode(imageObject.getString("url")),
            MediaContentType.IMAGE
        ).apply {
            metadata.resolutionX = imageObject.getInt("width")
            metadata.resolutionY = imageObject.getInt("height")
        }
    }

    private fun getMediaList(redditPost: RedditPost): List<MediaDownloadObject> {
        val image = redditPost.postData
            .getJSONObject("preview")
            .getJSONArray("images")
            .getJSONObject(0)

        val mediaDownloadList = mutableListOf(constructImage(image.getJSONObject("source")))
        val resolutions = image.getJSONArray("resolutions")
        for (i in 0 until resolutions.length()) {
            mediaDownloadList.add(constructImage(resolutions.getJSONObject(i)))
        }

        return mediaDownloadList
    }
}
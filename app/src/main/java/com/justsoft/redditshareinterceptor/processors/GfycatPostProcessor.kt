package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.mediaDownloadListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper
import java.util.regex.Pattern

class GfycatPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("gfycat.com")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.VIDEO

    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaDownloadList {
        val gfycatResponse = requestHelper
            .readHttpJsonResponse("$GFYCAT_API_GIF_REQUEST${getGfycatId(redditPost.url)}")
        val contentUrls = gfycatResponse
            .getJSONObject("gfyItem")
            .getJSONObject("content_urls")

        val allMediaList = mediaDownloadListOf(MediaContentType.VIDEO)

        val keysIterator = contentUrls.keys()
        while (keysIterator.hasNext()) {
            val formatType = keysIterator.next()
            if (formatType == "webp" || formatType == "webm" || formatType == "mobilePoster")
                continue

            val mediaObj = contentUrls.getJSONObject(formatType)

            allMediaList.add(
                MediaDownloadObject(
                    mediaObj.getString("url"),
                    MediaContentType.VIDEO,
                    mediaObj.getLong("size")
                )
            )
        }
        return allMediaList
    }

    private fun getGfycatId(sourceUrl: String): String {
        val matcher = Pattern.compile("com/([a-zA-Z0-9].+)").matcher(sourceUrl)
        matcher.find()
        return matcher.group(1)!!
    }

    companion object {
        private const val GFYCAT_API_GIF_REQUEST = "https://api.gfycat.com/v1/gfycats/"
    }
}
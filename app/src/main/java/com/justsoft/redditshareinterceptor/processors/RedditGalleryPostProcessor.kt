package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.GALLERY
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.IMAGE
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadList
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.mediaDownloadListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.urlDecode
import org.json.JSONObject

class RedditGalleryPostProcessor : PostProcessor {

    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        redditPost.url.contains("reddit.com/gallery")

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType =
        GALLERY

    override fun getAllPossibleMediaModels(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaDownloadList {
        return getMediaList(redditPost)
    }

    private fun constructImage(imageObject: JSONObject, galleryIndex: Int): MediaDownloadObject {
        return MediaDownloadObject(
            urlDecode(imageObject.getString("u")),
            IMAGE,
            galleryIndex
        ).apply {
            metadata.resolutionX = imageObject.getInt("x")
            metadata.resolutionY = imageObject.getInt("y")
        }
    }

    private fun getGalleryImageVariants(
        redditPost: RedditPost,
        imageItemId: String,
        galleryIndex: Int
    ): List<MediaDownloadObject> {
        val mediaObj = redditPost.postData
            .getJSONObject("media_metadata")
            .getJSONObject(imageItemId)

        val sourceObj = mediaObj.getJSONObject("s")
        val list = mutableListOf(constructImage(sourceObj, galleryIndex))

        val resolutions = mediaObj.getJSONArray("p")
        for (j in 0 until resolutions.length()) {
            list.add(constructImage(resolutions.getJSONObject(j), galleryIndex))
        }
        return list
    }

    private fun getMediaList(redditPost: RedditPost): MediaDownloadList {
        val mediaList = mediaDownloadListOf(GALLERY)

        val mediaMetadata = redditPost.postData.getJSONObject("media_metadata")
        val keysIterator = mediaMetadata.keys()
        var i = 0
        while (keysIterator.hasNext()) {
            val mediaId = keysIterator.next()
            mediaList.addAll(getGalleryImageVariants(redditPost, mediaId, i))
            i++
        }
        return mediaList
    }
}

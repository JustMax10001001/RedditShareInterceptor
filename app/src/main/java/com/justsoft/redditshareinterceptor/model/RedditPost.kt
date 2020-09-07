package com.justsoft.redditshareinterceptor.model

import org.json.JSONObject
import java.util.*

class RedditPost(
    private val postData: JSONObject
) {
    val url: String
        get() = postData.getString("url")

    val title: String
        get() = postData.getString("title")

    val selftext: String
        get() = postData.getString("selftext")

    val subreddit: String
        get() = postData.getString("subreddit_name_prefixed")

    val hasGallery: Boolean
        get() = postData.opt("media_metadata") != null

    val galleryImageUrls: List<String> by lazy {
            if (!hasGallery)
                throw IllegalStateException("This post does not contain media_metadata with image list")

            val urlList = mutableListOf<String>()
            val mediaMetadata = postData.getJSONObject("media_metadata")
            val keysIterator = mediaMetadata.keys()
            while (keysIterator.hasNext()) {
                val mediaId = keysIterator.next()
                val mediaObj = mediaMetadata.getJSONObject(mediaId)
                val mimeType = mediaObj.getString("m")
                var imageType = mimeType.replace("image/", "").toLowerCase(Locale.ROOT)
                if (imageType == "jpeg")
                    imageType = "jpg"
                urlList.add("https://i.redd.it/$mediaId.$imageType")
            }

            urlList
        }

    override fun toString(): String {
        return postData.toString()
    }
}
package com.justsoft.redditshareinterceptor.model

import com.justsoft.redditshareinterceptor.util.urlDecode
import org.json.JSONObject

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

    val hasPreviewImages: Boolean
        get() = postData.opt("preview") != null &&
                postData.getJSONObject("preview").opt("images") != null

    val previewImages: List<String> by lazy {
        if (!hasPreviewImages)
            throw IllegalStateException("This post does not contain image preview list")

        val urlList = mutableListOf<String>()

        val image = postData
            .getJSONObject("preview")
            .getJSONArray("images")
            .getJSONObject(0)

        urlList.add(urlDecode(image.getJSONObject("source").getString("url")))
        val resolutions = image.getJSONArray("resolutions")
        for (i in 0 until resolutions.length()) {
            urlList.add(urlDecode(resolutions.getJSONObject(i).getString("url")))
        }

        urlList
    }

    val hasGallery: Boolean
        get() = postData.opt("media_metadata") != null

    val galleryImageUrls: Map<Int, List<String>> by lazy {
        if (!hasGallery)
            throw IllegalStateException("This post does not contain media_metadata with image list")

        val urlList = mutableMapOf<Int, MutableList<String>>()
        val mediaMetadata = postData.getJSONObject("media_metadata")
        val keysIterator = mediaMetadata.keys()
        var i = 0
        while (keysIterator.hasNext()) {
            val mediaId = keysIterator.next()
            val mediaObj = mediaMetadata.getJSONObject(mediaId)
            urlList[i] = mutableListOf(urlDecode(mediaObj.getJSONObject("s").getString("u")))     // source

            val resolutions = mediaObj.getJSONArray("p")
            for (j in 0 until resolutions.length()) {
                urlList[i]?.add(urlDecode(resolutions.getJSONObject(j).getString("u")))
            }
            i++
        }

        urlList
    }

    override fun toString(): String {
        return postData.toString()
    }
}
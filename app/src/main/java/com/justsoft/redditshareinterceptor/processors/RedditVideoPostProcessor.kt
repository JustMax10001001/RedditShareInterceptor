package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.utils.MPDParser
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
import java.net.URL
import java.util.regex.Pattern

class RedditVideoPostProcessor : PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean =
        with(redditPost.url) {
            isVReddit()
        }

    /**
     * A property to temporary store the list of parsed MediaDownloadObjects
     * The class is expected to be stateless, however this requirement would require
     * parcelling the list into a bundle and back
     */
    private val mediaObjectCache: MutableMap<String, List<MediaDownloadObject>> = HashMap()

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType {
        val mpdBaseUrl = getMpdBaseUrl(redditPost.mediaDashUrl)

        val parser = MPDParser(mpdBaseUrl)

        val mediaList = parser.parse(
            URL(redditPost.mediaDashUrl).openStream()
        )
        mediaObjectCache[redditPost.url] = mediaList
        return parser.contentType
    }

    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> {
        return mediaObjectCache.remove(redditPost.url)!!
    }

    private fun getMpdBaseUrl(mediaDashUrl: String): String {
        return V_REDD_IT_PATTERN.matcher(mediaDashUrl).apply { find() }.group()
    }

    companion object {
        private val V_REDD_IT_PATTERN =
            Pattern.compile("(https://v.redd.it/[a-zA-Z0-9]*/)")
    }
}


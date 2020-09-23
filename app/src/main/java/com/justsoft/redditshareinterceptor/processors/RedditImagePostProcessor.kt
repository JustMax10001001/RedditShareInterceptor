package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaModel
import com.justsoft.redditshareinterceptor.model.media.mediaListOf
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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
    ): MediaList =
        getMediaList(redditPost, requestHelper)


    private fun getMediaList(redditPost: RedditPost, requestHelper: RequestHelper): MediaList {
        return mediaListOf(MediaContentType.IMAGE).apply {
            runBlocking(Dispatchers.IO) {
                redditPost.previewImages.forEach {
                    add(MediaModel(it, MediaContentType.IMAGE, requestHelper.getContentLength(it)))
                }
            }
        }
    }
}
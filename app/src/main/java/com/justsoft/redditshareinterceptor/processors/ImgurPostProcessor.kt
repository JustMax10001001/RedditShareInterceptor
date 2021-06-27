package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.util.request.RequestHelper

class ImgurPostProcessor: PostProcessor {
    override fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean = with(redditPost.url) {
        isIImgur() && hasGifExtension()
    }

    override fun getPostContentType(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType = MediaContentType.VIDEO

    override fun getAllPossibleMediaDownloadObjects(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> {
        return listOf(MediaDownloadObject(redditPost.url.replace(".gifv", ".mp4"), MediaContentType.VIDEO))
    }
}
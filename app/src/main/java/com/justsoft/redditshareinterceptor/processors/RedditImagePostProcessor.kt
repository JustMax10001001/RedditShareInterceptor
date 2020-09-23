package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import android.os.ParcelFileDescriptor
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.*
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


    override fun downloadMediaMatchingMediaSpec(
        redditPost: RedditPost,
        savedState: Bundle,
        requestHelper: RequestHelper,
        mediaSpec: MediaSpec,
        destinationDescriptorGenerator: (MediaContentType, Int) -> ParcelFileDescriptor
    ): MediaList {
        val image = getMediaList(redditPost, requestHelper)
        requestHelper.downloadFile(
            image[0].downloadUrl,
            destinationDescriptorGenerator(MediaContentType.IMAGE, 0)
        )
        return image
    }

    private fun getMediaList(redditPost: RedditPost, requestHelper: RequestHelper): MediaList {
        return mediaListOf(MediaContentType.IMAGE).apply {
            runBlocking(Dispatchers.IO) {
                redditPost.previewImages.forEach {
                    add(MediaModel(it, requestHelper.getContentLength(it), MediaContentType.IMAGE))
                }
            }
        }
    }
}
package com.justsoft.redditshareinterceptor.websitehandlers

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.MultipleSuitableProcessorsExceptions
import com.justsoft.redditshareinterceptor.PostContentTypeAcquiringException
import com.justsoft.redditshareinterceptor.PostContentUrlAcquiringException
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadInfo
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.Stopwatch
import java.util.regex.Pattern

class RedditUrlHandler : UrlHandler {

    private val redditPostProcessors: List<PostProcessor> = listOf(
        RedditImagePostProcessor(),
        GfycatPostProcessor(),
        RedditVideoPostProcessor(),
        RedditTextPostProcessor(),
        RedGifsPostProcessor(),
        RedditGalleryPostProcessor(),
        StreamablePostProcessor()
    )

    private val unknownPostProcessor = UnknownContentUrlPostProcessor()

    override fun isHandlerSuitableForUrl(url: String): Boolean =
        Pattern.compile("(https://www\\.reddit\\.com/r/\\w*/comments/\\w+/)").matcher(url).find()

    /**
     * @return fully-featured MediaDownloadInfo with all possible downloads, e.g. unfiltered download list
     */
    override fun processUrl(url: String, requestHelper: RequestHelper): MediaDownloadInfo {
        val stopwatch = Stopwatch()

        val cleanUrl = getPostApiUrl(url)
        Log.d(LOG_TAG, "Got post api link $cleanUrl")

        stopwatch.start()
        val redditPost = downloadRedditPost(cleanUrl, requestHelper)
        Log.d(LOG_TAG, "Downloaded post object in ${stopwatch.restartAndGetTimeElapsed()} ms")

        val postProcessor = selectPostProcessor(redditPost)
        Log.d(
            LOG_TAG,
            "Selected Reddit processor ${postProcessor.javaClass.simpleName} in ${stopwatch.restartAndGetTimeElapsed()} ms"
        )

        val postProcessorBundle = Bundle()
        val postContentType = getPostMediaType(
            postProcessor, redditPost, postProcessorBundle, requestHelper
        )
        Log.d(
            LOG_TAG,
            "Got post content type $postContentType in ${stopwatch.restartAndGetTimeElapsed()} ms"
        )
        FirebaseAnalyticsHelper.getInstance().logEvent("get_reddit_media_type") {
            param("clean_url", cleanUrl)
            param("processor_name", postProcessor.javaClass.simpleName)
            param("content_type", postContentType.toString())
        }

        val unfilteredMedia = getUnfilteredMedia(
            postProcessor,
            redditPost,
            postProcessorBundle,
            requestHelper
        )
        Log.d(
            LOG_TAG,
            "Got post media list in ${stopwatch.restartAndGetTimeElapsed()} ms"
        )

        val caption =
            postProcessor.getPostCaption(redditPost, postProcessorBundle, requestHelper)

        return MediaDownloadInfo(
            postContentType, caption, unfilteredMedia
        )
    }

    private fun getUnfilteredMedia(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        requestHelper: RequestHelper
    ): List<MediaDownloadObject> {
        return try {
            postProcessor.getAllPossibleMediaDownloadObjects(
                postObject,
                postProcessorBundle,
                requestHelper
            )
        } catch (e: Exception) {
            throw PostContentUrlAcquiringException(cause = e)
        }
    }

    private fun getPostMediaType(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        requestHelper: RequestHelper
    ): MediaContentType {
        return try {
            postProcessor.getPostContentType(
                postObject, postProcessorBundle, requestHelper
            )
        } catch (e: Exception) {
            throw PostContentTypeAcquiringException(cause = e)
        }
    }

    fun selectPostProcessor(redditPost: RedditPost): PostProcessor {
        val suitableProcessors = mutableSetOf<PostProcessor>()
        for (processor in redditPostProcessors) {
            if (processor.isProcessorSuitableForPost(redditPost)) {
                suitableProcessors.add(processor)
            }
        }
        return when (suitableProcessors.size) {
            0 -> unknownPostProcessor
            1 -> suitableProcessors.first()
            else -> throw MultipleSuitableProcessorsExceptions(processors = suitableProcessors)
        }
    }

    private fun getPostApiUrl(url: String): String {
        val pattern = Pattern.compile("(https://www\\.reddit\\.com/r/\\w*/comments/(\\w+)/)")
        val id = pattern.matcher(url).apply { this.find() }.group(2)!!
        return "https://www.reddit.com/by_id/t3_$id/"
    }

    fun downloadRedditPost(postUrl: String, requestHelper: RequestHelper): RedditPost {
        val response = requestHelper.readHttpJsonResponse("$postUrl.json")
        return RedditPost(
            response.getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data")
        )
    }

    companion object {
        private const val LOG_TAG = "RedditHandler"
    }
}
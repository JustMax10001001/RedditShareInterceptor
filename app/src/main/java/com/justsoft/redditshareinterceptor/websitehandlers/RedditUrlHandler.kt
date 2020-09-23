package com.justsoft.redditshareinterceptor.websitehandlers

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.MultipleSuitableProcessorsExceptions
import com.justsoft.redditshareinterceptor.NoSuitableProcessorException
import com.justsoft.redditshareinterceptor.PostContentTypeAcquiringException
import com.justsoft.redditshareinterceptor.PostContentUrlAcquiringException
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import org.json.JSONArray
import java.util.regex.Pattern

class RedditUrlHandler : UrlHandler {

    private val redditPostProcessors: List<PostProcessor> = listOf(
        RedditImagePostProcessor(),
        //ImgurImageProcessor(),
        GfycatPostProcessor(),
        RedditVideoPostProcessor(),
        RedditTextPostProcessor(),
        RedGifsPostProcessor(),
        RedditGalleryPostProcessor(),
        StreamablePostProcessor(),
        RedditTwitterPostProcessor()
    )

    override fun isHandlerSuitableForUrl(url: String): Boolean =
        url.matches(Regex.fromLiteral("https://www\\.reddit\\.com/r/\\w*/comments/\\w+/"))

    override fun processUrlAndGetMedia(url: String, requestHelper: RequestHelper): MediaList {
        val cleanUrl = extractCleanUrl(url)
        val redditPost = downloadRedditPost(cleanUrl, requestHelper)

        val postProcessor = selectPostProcessor(redditPost)
        Log.d(LOG_TAG, "Selected Reddit processor ${postProcessor.javaClass.simpleName}")

        val postProcessorBundle = Bundle()
        val postContentType = getPostMediaType(
            postProcessor, redditPost, postProcessorBundle, requestHelper
        )
        Log.d(LOG_TAG, "Post content type is $postContentType")
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

        unfilteredMedia.caption =
            postProcessor.getPostCaption(redditPost, postProcessorBundle, requestHelper)

        return unfilteredMedia
    }

    private fun getUnfilteredMedia(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        requestHelper: RequestHelper
    ): MediaList {
        return try {
            postProcessor.getAllPossibleMediaModels(
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
        when (suitableProcessors.size) {
            0 -> throw  NoSuitableProcessorException("Post url is ${redditPost.url}")
            1 -> return suitableProcessors.first()
            else -> throw MultipleSuitableProcessorsExceptions(processors = suitableProcessors)
        }
    }

    private fun extractCleanUrl(url: String): String {
        val pattern = Pattern.compile("(https://www\\.reddit\\.com/r/\\w*/comments/\\w+/)")
        return pattern.matcher(url).apply { this.find() }.group()
    }

    private fun downloadRedditPost(postUrl: String, requestHelper: RequestHelper): RedditPost =
        RedditPost(
            JSONArray(requestHelper.readHttpTextResponse("$postUrl.json"))
                .getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data")
        )

    companion object {
        private const val LOG_TAG = "RedditHandler"
    }
}
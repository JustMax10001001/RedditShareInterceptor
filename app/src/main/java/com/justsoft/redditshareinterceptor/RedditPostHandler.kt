package com.justsoft.redditshareinterceptor

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaSpec
import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import org.json.JSONArray
import java.util.regex.Pattern

class RedditPostHandler(
    private val requestHelper: RequestHelper,
    private val createDestinationFileDescriptor: (MediaContentType, Int) -> ParcelFileDescriptor
) {

    private val postProcessors = mutableListOf<PostProcessor>()

    private var onMediaDownloaded: (MediaContentType, RedditPost, Int) -> Unit = { _, _, _ -> }
    private var onTextPost: (RedditPost, String) -> Unit = { _, _ -> }
    private var onError: (Throwable) -> Unit = { }

    init {
        postProcessors.addAll(
            listOf(
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
        )
    }

    fun extractSimpleUrl(url: String): String {
        val pattern = Pattern.compile("(https://www\\.reddit\\.com/r/\\w*/comments/\\w+/)")
        return pattern.matcher(url).apply { this.find() }.group()
    }

    fun handlePostUrl(dirtyUrl: String) {
        val postUrl = extractSimpleUrl(dirtyUrl)

        Log.d(LOG_TAG, "Starting executor")
        try {
            Log.d(LOG_TAG, "Starting to process the post")
            getAndProcessRedditPost(postUrl)
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun getAndProcessRedditPost(cleanUrl: String) {
        val postObject = getRedditPostObj(cleanUrl)

        val postProcessor = selectPostProcessor(postObject)
        Log.d(LOG_TAG, "Selected processor ${postProcessor.javaClass.simpleName}")
        FirebaseAnalyticsHelper.getInstance().logEvent("select_post_processor") {
            param("clean_url", cleanUrl)
            param("processor_name", postProcessor.javaClass.simpleName)
        }

        val postProcessorBundle = Bundle()
        val postContentType = getPostMediaType(postProcessor, postObject, postProcessorBundle)
        Log.d(LOG_TAG, "Post content type is $postContentType")
        FirebaseAnalyticsHelper.getInstance().logEvent("get_media_type") {
            param("clean_url", cleanUrl)
            param("content_type", postContentType.toString())
        }

        if (postContentType != MediaContentType.TEXT) {
            val filesDownloaded = downloadPostMedia(postProcessor, postObject, postProcessorBundle)
            onMediaDownloaded(postContentType, postObject, filesDownloaded)
        } else {
            val caption = getTextFromProcessor(
                postProcessor, postObject, postProcessorBundle
            )
            onTextPost(postObject, caption)
        }
    }

    private fun getTextFromProcessor(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        mediaSpec: MediaSpec = MediaSpec()
    ): String {
        return postProcessor.getAllPossibleMediaModels(
            postObject,
            postProcessorBundle,
            requestHelper
        )[0].caption
    }

    private fun downloadPostMedia(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        mediaSpec: MediaSpec = MediaSpec()
    ): Int {
        return try {
            postProcessor.getAllPossibleMediaModels(
                postObject,
                postProcessorBundle,
                requestHelper
            ).count()
        } catch (e: Exception) {
            throw MediaDownloadException(cause = e)
        }
    }

    private fun getPostMediaType(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle
    ): MediaContentType {
        return try {
            postProcessor.getPostContentType(
                postObject, postProcessorBundle, requestHelper
            )
        } catch (e: Exception) {
            throw PostContentTypeAcquiringException(cause = e)
        }
    }

    fun mediaSuccess(onMediaDownloaded: (MediaContentType, RedditPost, Int) -> Unit) {
        this.onMediaDownloaded = onMediaDownloaded
    }

    fun textSuccess(onTextSuccess: (RedditPost, String) -> Unit) {
        this.onTextPost = onTextSuccess
    }

    fun error(onError: (Throwable) -> Unit) {
        this.onError = onError
    }

    fun getRedditPostObj(postUrl: String): RedditPost =
        RedditPost(
            JSONArray(requestHelper.readHttpTextResponse("$postUrl.json"))
                .getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data")
        )


    fun selectPostProcessor(redditPost: RedditPost): PostProcessor {
        val suitableProcessors = mutableSetOf<PostProcessor>()
        for (processor in postProcessors) {
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

    companion object {
        const val LOG_TAG = "PostHandler"
    }
}
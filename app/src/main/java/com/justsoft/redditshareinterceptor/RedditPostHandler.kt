package com.justsoft.redditshareinterceptor

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.util.RequestHelper
import org.json.JSONArray
import java.io.FileDescriptor
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.regex.Pattern

class RedditPostHandler(private val requestHelper: RequestHelper) {

    private val postProcessors = mutableListOf<PostProcessor>()

    private var onMediaDownloaded: (ContentType, RedditPost) -> Unit = { _, _ -> }
    private var onTextPost: (RedditPost) -> Unit = { }
    private var onError: (Throwable) -> Unit = { }

    init {
        postProcessors.addAll(
            listOf(
                RedditImagePostProcessor(),
                ImgurImageProcessor(),
                GfycatPostProcessor(),
                RedditVideoPostProcessor(),
                TextPostProcessor(),
                RedGifsPostProcessor(),
            )
        )
    }

    fun extractSimpleUrl(url: String): String {
        val pattern = Pattern.compile("(https://www\\.reddit\\.com/r/\\w*/comments/\\w+/)")
        return pattern.matcher(url).apply { this.find() }.group()
    }

    fun handlePostUrl(
        dirtyUrl: String,
        createDestinationFileDescriptor: (ContentType) -> FileDescriptor
    ) {
        val postUrl = extractSimpleUrl(dirtyUrl)

        Log.d(LOG_TAG, "Starting executor")
        try {
            Log.d(LOG_TAG, "Starting to process the post")
            getAndProcessRedditPost(
                postUrl,
                createDestinationFileDescriptor
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun getAndProcessRedditPost(
        cleanUrl: String,
        createDestinationFileDescriptor: (ContentType) -> FileDescriptor
    ) {
        val postObject = getRedditPostObj(cleanUrl)

        val postProcessor = selectPostProcessor(postObject)
        Log.d(LOG_TAG, "Selected processor ${postProcessor.javaClass.simpleName}")
        val postProcessorBundle = Bundle()
        val postContentType = try {
            postProcessor.getPostContentType(
                postObject, postProcessorBundle, requestHelper
            )
        } catch (e: Exception) {
            throw PostContentTypeAcquiringException(cause = e)
        }
        Log.d(LOG_TAG, "Post content type is $postContentType")
        if (postContentType != ContentType.TEXT) {
            val postMediaUrl = try {
                postProcessor.getMediaDownloadUrl(
                    postObject, postProcessorBundle, requestHelper
                )
            } catch (e: Exception) {
                throw PostContentUrlAcquiringException(cause = e)
            }
            Log.d(LOG_TAG, "Post media download url is $postMediaUrl")
            val fileDescriptor = try {
                createDestinationFileDescriptor(postContentType)
            } catch (e: Exception) {
                throw DescriptorCreationException(cause = e)
            }
            try {
                requestHelper.downloadFile(postMediaUrl, fileDescriptor)
            } catch (e: Exception) {
                throw MediaDownloadException(cause = e)
            }
            Log.d(LOG_TAG, "Post successfully downloaded")
             onMediaDownloaded(postContentType, postObject)
        } else {
            onTextPost(postObject)
        }
    }

    fun mediaSuccess(onMediaDownloaded: (ContentType, RedditPost) -> Unit) {
        this.onMediaDownloaded = onMediaDownloaded
    }

    fun textSuccess(onTextSuccess: (RedditPost) -> Unit) {
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
            0 -> throw  NoSuitableProcessorException()
            1 -> return suitableProcessors.first()
            else -> throw MultipleSuitableProcessorsExceptions(processors = suitableProcessors)
        }
    }

    companion object {
        const val LOG_TAG = "PostHandler"
    }
}
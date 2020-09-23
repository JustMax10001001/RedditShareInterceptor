package com.justsoft.redditshareinterceptor

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.downloaders.*
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.*
import com.justsoft.redditshareinterceptor.model.media.MediaList
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

    private val postProcessors: List<PostProcessor> = listOf(
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

    private val downloaders: Map<MediaContentType, MediaDownloader> = mapOf(
        GIF to GifDownloader(),
        VIDEO to VideoDownloader(),
        IMAGE to ImageDownloader(),
        GALLERY to GalleryDownloader()
    )


    private var onMediaDownloaded: (MediaContentType, RedditPost, Int) -> Unit = { _, _, _ -> }
    private var onTextPost: (RedditPost, String) -> Unit = { _, _ -> }
    private var onError: (Throwable) -> Unit = { }

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

        if (postContentType != TEXT) {
            val unfilteredMedia = getUnfilteredMedia(postProcessor, postObject, postProcessorBundle)
            Log.d(LOG_TAG, "Got unfiltered media, count: ${unfilteredMedia.count()}")

            val filteredMediaList = filterMedia(unfilteredMedia, MediaSpec())
            Log.d(LOG_TAG, "Filtered media, count: ${filteredMediaList.count()}")

            val downloadedMediaCount = downloadMedia(filteredMediaList)
            Log.d(LOG_TAG, "Downloaded media, count: $downloadedMediaCount")

            onMediaDownloaded(postContentType, postObject, downloadedMediaCount)
        } else {
            Log.d(LOG_TAG, "Got text from processor")
            val caption = getTextFromProcessor(
                postProcessor, postObject, postProcessorBundle
            )
            onTextPost(postObject, caption)
        }
    }

    private fun getTextFromProcessor(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle
    ): String {
        return postProcessor.getAllPossibleMediaModels(
            postObject,
            postProcessorBundle,
            requestHelper
        )[0].caption
    }

    private fun downloadMedia(
        filteredMediaList: MediaList
    ): Int {
        return try {
            selectDownloaderForMediaType(filteredMediaList.listMediaContentType)
                .downloadMedia(filteredMediaList, requestHelper, createDestinationFileDescriptor)
        }catch (e: Exception) {
            throw MediaDownloadException(cause = e)
        }
    }

    private fun filterMedia(
        unfilteredMediaList: MediaList,
        filterSpec: MediaSpec
    ): MediaList =
        try {
            unfilteredMediaList.getMostSuitableMedia(filterSpec)
        } catch (e: Exception) {
            throw MediaFilterException(cause = e)
        }

    private fun getUnfilteredMedia(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle
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

    private fun selectDownloaderForMediaType(mediaContentType: MediaContentType): MediaDownloader =
        downloaders[mediaContentType] ?: error("No downloader for type $mediaContentType")

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
        private const val LOG_TAG = "PostHandler"
    }
}
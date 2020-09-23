package com.justsoft.redditshareinterceptor

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.processors.RedditGalleryPostProcessor.Companion.KEY_GET_URL_OF_IMAGE_INDEX
import com.justsoft.redditshareinterceptor.processors.RedditGalleryPostProcessor.Companion.KEY_IMAGES_COUNT
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.util.regex.Pattern

class RedditPostHandler(private val requestHelper: RequestHelper) {

    private val postProcessors = mutableListOf<PostProcessor>()

    private var onMediaDownloaded: (ContentType, RedditPost, Int) -> Unit = { _, _, _ -> }
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
                RedditGalleryPostProcessor(),
                StreamablePostProcessor(),
            )
        )
    }

    fun extractSimpleUrl(url: String): String {
        val pattern = Pattern.compile("(https://www\\.reddit\\.com/r/\\w*/comments/\\w+/)")
        return pattern.matcher(url).apply { this.find() }.group()
    }

    fun handlePostUrl(
        dirtyUrl: String,
        createDestinationFileDescriptor: (ContentType, Int) -> ParcelFileDescriptor
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
        createDestinationFileDescriptor: (ContentType, Int) -> ParcelFileDescriptor
    ) {
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
            param("processor_name", postContentType.toString())
        }

        when (postContentType) {
            ContentType.GALLERY -> {
                val count = processMediaMultiple(
                    postProcessor,
                    postObject,
                    postProcessorBundle,
                    createDestinationFileDescriptor,
                    postContentType
                )
                onMediaDownloaded(postContentType, postObject, count)
            }
            ContentType.TEXT -> onTextPost(postObject)
            else -> {
                processMediaSingle(
                    postProcessor,
                    postObject,
                    postProcessorBundle,
                    createDestinationFileDescriptor,
                    postContentType
                )

                onMediaDownloaded(postContentType, postObject, 1)
            }
        }
    }

    private fun processMediaMultiple(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        createDestinationFileDescriptor: (ContentType, Int) -> ParcelFileDescriptor,
        postContentType: ContentType
    ): Int {
        val imageCount = postProcessorBundle.getInt(KEY_IMAGES_COUNT)
        runBlocking {
            for (i in 0 until imageCount) {
                postProcessorBundle.putInt(KEY_GET_URL_OF_IMAGE_INDEX, i)
                val postMediaUrl =
                    getMediaFileDownloadUrl(postProcessor, postObject, postProcessorBundle)
                val fileDescriptor =
                    createMediaFileDescriptor(createDestinationFileDescriptor, postContentType, i)
                launch(Dispatchers.IO) {
                    Log.d(LOG_TAG, "Image ${i + 1}'s media download url is $postMediaUrl")
                    downloadMedia(postMediaUrl, fileDescriptor)
                    Log.d(LOG_TAG, "Downloaded ${i + 1} images of $imageCount")
                }
            }
        }
        return imageCount
    }

    private fun processMediaSingle(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle,
        createDestinationFileDescriptor: (ContentType, Int) -> ParcelFileDescriptor,
        postContentType: ContentType
    ) {
        val postMediaUrl =
            getMediaFileDownloadUrl(postProcessor, postObject, postProcessorBundle)
        Log.d(LOG_TAG, "Post media download url is $postMediaUrl")

        val fileDescriptor =
            createMediaFileDescriptor(createDestinationFileDescriptor, postContentType)
        downloadMedia(postMediaUrl, fileDescriptor)
        Log.d(LOG_TAG, "Post media successfully downloaded")
    }

    private fun getPostMediaType(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle
    ): ContentType {
        return try {
            postProcessor.getPostContentType(
                postObject, postProcessorBundle, requestHelper
            )
        } catch (e: Exception) {
            throw PostContentTypeAcquiringException(cause = e)
        }
    }

    private fun getMediaFileDownloadUrl(
        postProcessor: PostProcessor,
        postObject: RedditPost,
        postProcessorBundle: Bundle
    ): String {
        return try {
            postProcessor.getMediaDownloadUrl(
                postObject, postProcessorBundle, requestHelper
            )
        } catch (e: Exception) {
            throw PostContentUrlAcquiringException(cause = e)
        }
    }

    private fun createMediaFileDescriptor(
        createDestinationFileDescriptor: (ContentType, Int) -> ParcelFileDescriptor,
        postContentType: ContentType,
        mediaIndex: Int = 0
    ): ParcelFileDescriptor {
        return try {
            createDestinationFileDescriptor(postContentType, mediaIndex)
        } catch (e: Exception) {
            throw DescriptorCreationException(cause = e)
        }
    }

    fun downloadMedia(
        postMediaUrl: String,
        fileDescriptor: ParcelFileDescriptor
    ) {
        try {
            requestHelper.downloadFile(postMediaUrl, fileDescriptor)
        } catch (e: Exception) {
            throw MediaDownloadException(cause = e)
        }
    }

    fun mediaSuccess(onMediaDownloaded: (ContentType, RedditPost, Int) -> Unit) {
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
package com.justsoft.redditshareinterceptor

import android.net.Uri
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaSpec
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.Stopwatch
import com.justsoft.redditshareinterceptor.websitehandlers.RedditUrlHandler
import com.justsoft.redditshareinterceptor.websitehandlers.UrlHandler
import java.io.OutputStream

class UniversalUrlProcessor(
    private val requestHelper: RequestHelper,
    createUri: (MediaContentType, Int) -> Uri,
    openOutputStream: (Uri) -> OutputStream
) {

    private var onUrlProcessed: (ProcessingResult) -> Unit = { }
    private var onError: (Throwable) -> Unit = { }

    private val downloader = UniversalMediaDownloader(requestHelper, createUri, openOutputStream)

    private val websiteHandlers = listOf(
        RedditUrlHandler()
    )

    fun handleUrl(url: String, progressCallback: (ProcessingProgress) -> Unit) {
        val stopwatch = Stopwatch()
        Log.d(LOG_TAG, "Starting url processing")
        stopwatch.start()
        val result = try {
            startUrlProcessing(url, progressCallback)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Processing failed in ${stopwatch.stopAndGetTimeElapsed()} ms")
            return onError(e)
        }
        Log.d(LOG_TAG, "Processing succeeded in ${stopwatch.stopAndGetTimeElapsed()} ms")
        result.processingTime = stopwatch.timeElapsed()
        onUrlProcessed(result)
    }

    private fun startUrlProcessing(
        url: String,
        progressCallback: (ProcessingProgress) -> Unit
    ): ProcessingResult {
        val urlHandler = selectUrlHandler(url)
        FirebaseAnalyticsHelper.getInstance().logEvent("select_url_handler") {
            param("url", url)
            param("handler_name", urlHandler.javaClass.simpleName)
        }
        progressCallback(ProcessingProgress(R.string.processing_media_state_found_url_handler, 5))

        val unfilteredMediaList = urlHandler.processUrlAndGetMedia(url, requestHelper)
        Log.d(LOG_TAG, "Got unfiltered media, count: ${unfilteredMediaList.count()}")
        progressCallback(
            ProcessingProgress(
                R.string.processing_media_state_loaded_media_variants,
                35
            )
        )

        val filteredMediaList = filterMedia(unfilteredMediaList, MediaSpec())
        Log.d(LOG_TAG, "Filtered media, count: ${filteredMediaList.count()}")
        progressCallback(
            ProcessingProgress(
                R.string.processing_media_state_filtered_media_variants,
                40
            )
        )

        val uris = if (filteredMediaList.listMediaContentType != MediaContentType.TEXT) {
            downloadMedia(filteredMediaList) { progress: ProcessingProgress ->
                progressCallback(
                    ProcessingProgress(
                        R.string.processing_media_state_downloading_media,
                        40 + (0.6 * progress.overallProgress).toInt()
                    )
                )
            }
        } else {
            emptyList()
        }
        if (uris.isNotEmpty())
            Log.d(LOG_TAG, "Downloaded media files, count: ${uris.count()}")

        return ProcessingResult(
            filteredMediaList.listMediaContentType,
            filteredMediaList.caption,
            uris
        )
    }

    private fun downloadMedia(
        filteredMediaList: MediaList,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ): List<Uri> {
        return try {
            downloader.downloadMediaList(filteredMediaList, downloadProgressCallback)
        } catch (e: Exception) {
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

    private fun selectUrlHandler(url: String): UrlHandler {
        val suitableHandlers = mutableSetOf<UrlHandler>()
        for (handler in websiteHandlers) {
            if (handler.isHandlerSuitableForUrl(url)) {
                suitableHandlers.add(handler)
            }
        }
        when (suitableHandlers.size) {
            0 -> throw  NoSuitableUrlHandlerException("Url is $url")
            1 -> return suitableHandlers.first()
            else -> throw MultipleSuitableUrlHandlersExceptions(handlers = suitableHandlers)
        }
    }

    fun error(onError: (Throwable) -> Unit) {
        this.onError = onError
    }

    fun result(onResult: (ProcessingResult) -> Unit) {
        this.onUrlProcessed = onResult
    }

    companion object {
        private const val LOG_TAG = "UUrlProcessor"
    }
}
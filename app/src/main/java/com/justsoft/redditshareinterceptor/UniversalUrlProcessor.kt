package com.justsoft.redditshareinterceptor

import android.net.Uri
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.media.*
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.Stopwatch
import com.justsoft.redditshareinterceptor.websitehandlers.RedditUrlHandler
import com.justsoft.redditshareinterceptor.websitehandlers.UrlHandler
import java.io.OutputStream

class UniversalUrlProcessor(
    private val requestHelper: RequestHelper,
    private val createUri: (MediaContentType, Int) -> Uri,
    openOutputStream: (Uri) -> OutputStream
) {

    private var onProcessingFinished: (ProcessingResult) -> Unit = { }

    private val downloader = UniversalMediaDownloader(requestHelper, openOutputStream)

    private val websiteHandlers = listOf(
        RedditUrlHandler()
    )

    fun handleUrl(url: String, progressCallback: (ProcessingProgress) -> Unit) {
        val stopwatch = Stopwatch()
        Log.d(LOG_TAG, "Starting url processing")
        stopwatch.start()
        val processingResult = try {
            val info = startUrlProcessing(url, progressCallback)
            val delta = stopwatch.stopAndGetTimeElapsed()
            Log.d(LOG_TAG, "Processing succeeded in $delta ms")

            ProcessingResult.success(info, delta)
        } catch (e: Exception) {
            val delta = stopwatch.stopAndGetTimeElapsed()
            Log.e(LOG_TAG, "Processing failed in $delta ms", e)

            ProcessingResult.error(e, delta)
        }
        onProcessingFinished(processingResult)
    }

    private fun startUrlProcessing(
        url: String,
        progressCallback: (ProcessingProgress) -> Unit
    ): MediaDownloadInfo {
        val urlHandler = selectUrlHandler(url)
        FirebaseAnalyticsHelper.getInstance().logEvent("select_url_handler") {
            param("url", url)
            param("handler_name", urlHandler.javaClass.simpleName)
        }
        progressCallback(ProcessingProgress(R.string.processing_media_state_found_url_handler, 5))

        val mediaDownloadInfo = urlHandler.processUrl(url, requestHelper)
        Log.d(
            LOG_TAG,
            "Got unfiltered media, count: ${mediaDownloadInfo.mediaDownloadList.count()}"
        )
        progressCallback(
            ProcessingProgress(
                R.string.processing_media_state_loaded_media_variants,
                35
            )
        )

        val filteredMediaList = filterMedia(
            mediaDownloadInfo.mediaDownloadList,
            mediaDownloadInfo.mediaContentType,
            MediaQualitySpec.PRESET_HIGH
        )
        Log.d(LOG_TAG, "Filtered media, count: ${filteredMediaList.count()}")
        progressCallback(
            ProcessingProgress(
                R.string.processing_media_state_filtered_media_variants,
                40
            )
        )
        mediaDownloadInfo.mediaDownloadList.clear()
        mediaDownloadInfo.mediaDownloadList.addAll(filteredMediaList)

        if (mediaDownloadInfo.mediaContentType != MediaContentType.TEXT) {
            generateDestinationUris(mediaDownloadInfo)
            Log.d(LOG_TAG, "Generated destination Uris")

            downloadMedia(mediaDownloadInfo) { progress: ProcessingProgress ->
                progressCallback(
                    ProcessingProgress(
                        R.string.processing_media_state_downloading_media,
                        40 + (0.6 * progress.overallProgress).toInt()
                    )
                )
            }
        }

        if (filteredMediaList.isNotEmpty())
            Log.d(LOG_TAG, "Downloaded media files, count: ${filteredMediaList.count()}")

        return mediaDownloadInfo
    }

    private fun generateDestinationUris(filteredDownloadInfo: MediaDownloadInfo) {
        try {
            filteredDownloadInfo.mediaDownloadList.forEach { media ->
                media.metadata.uri = createUri(
                    filteredDownloadInfo.mediaContentType,
                    media.galleryIndex
                )
            }
        } catch (e: Exception) {
            throw UriCreationException(cause = e)
        }
    }

    private fun downloadMedia(
        filteredMediaInfo: MediaDownloadInfo,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        return try {
            downloader.downloadMediaList(filteredMediaInfo, downloadProgressCallback)
        } catch (e: Exception) {
            throw MediaDownloadException(cause = e)
        }
    }

    private fun filterMedia(
        unfilteredDownloadList: MutableList<MediaDownloadObject>,
        contentType: MediaContentType,
        filterSpec: MediaQualitySpec
    ): List<MediaDownloadObject> =
        try {
            MediaListFilter.filterListByQualitySpec(filterSpec, unfilteredDownloadList, contentType)
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

    fun finished(onResult: (ProcessingResult) -> Unit) {
        this.onProcessingFinished = onResult
    }

    companion object {
        private const val LOG_TAG = "UUrlProcessor"
    }
}
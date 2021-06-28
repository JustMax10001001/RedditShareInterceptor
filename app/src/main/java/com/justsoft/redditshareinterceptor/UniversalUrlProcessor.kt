package com.justsoft.redditshareinterceptor

import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.emit
import com.justsoft.redditshareinterceptor.model.media.*
import com.justsoft.redditshareinterceptor.model.media.MediaContentType.*
import com.justsoft.redditshareinterceptor.providers.media.MediaPathProvider
import com.justsoft.redditshareinterceptor.services.io.FileIoService
import com.justsoft.redditshareinterceptor.services.media.MediaDownloadService
import com.justsoft.redditshareinterceptor.utils.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.utils.Stopwatch
import com.justsoft.redditshareinterceptor.utils.combineVideoAndAudio
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
import com.justsoft.redditshareinterceptor.websitehandlers.RedditUrlHandler
import com.justsoft.redditshareinterceptor.websitehandlers.UrlHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

class UniversalUrlProcessor @Inject constructor(
    private val requestHelper: RequestHelper,
    private val mediaPathProvider: MediaPathProvider,
    private val fileIoService: FileIoService,
    private val downloadService: MediaDownloadService
) {
    private var onProcessingFinished: (ProcessingResult) -> Unit = { }

    private val websiteHandlers = listOf(
        RedditUrlHandler()
    )

    suspend fun handleUrl(
        url: String
    ): Flow<ProcessingProgress> = flow {
        Log.d(LOG_TAG, "Starting url processing")
        val stopwatch = Stopwatch().apply { start() }
        val processingResult = try {
            val info = startUrlProcessing(url, this)
            val delta = stopwatch.stopAndGetTimeElapsed()
            Log.d(LOG_TAG, "Processing succeeded in $delta ms")

            FirebaseAnalyticsHelper.getAnalytics().logEvent("url_processed") {
                param("website_handler", selectUrlHandler(url)::class.java.simpleName)
                param("media_type", info.mediaContentType.toString())
                param("media_count", info.mediaDownloadList.count().toLong())
                param("processing_time", delta)
            }

            ProcessingResult.success(info, delta)
        } catch (e: Exception) {
            val delta = stopwatch.stopAndGetTimeElapsed()
            Log.e(LOG_TAG, "Processing failed in $delta ms", e)

            ProcessingResult.error(e, delta)
        }
        onProcessingFinished(processingResult)
    }

    private suspend fun startUrlProcessing(
        url: String,
        flowCollector: FlowCollector<ProcessingProgress>
    ): MediaDownloadInfo {
        val urlHandler = selectUrlHandler(url)
        flowCollector.emit(R.string.processing_media_state_found_url_handler, 5)
        Log.d(LOG_TAG, "Selected URL handler \"${urlHandler.javaClass.simpleName}\"")

        val mediaDownloadInfo = urlHandler.processUrl(url, requestHelper)
        Log.d(
            LOG_TAG,
            "Got unfiltered media, count: ${mediaDownloadInfo.mediaDownloadList.count()}"
        )
        flowCollector.emit(
            R.string.processing_media_state_loaded_media_variants,
            35
        )

        val filteredMediaList = filterMedia(
            mediaDownloadInfo.mediaDownloadList,
            mediaDownloadInfo.mediaContentType,
            MediaQualitySpec.PRESET_HIGH
        )
        Log.d(LOG_TAG, "Filtered media, count: ${filteredMediaList.count()}")
        flowCollector.emit(
            R.string.processing_media_state_filtered_media_variants,
            40
        )
        mediaDownloadInfo.mediaDownloadList.clear()
        mediaDownloadInfo.mediaDownloadList.addAll(filteredMediaList)

        if (mediaDownloadInfo.mediaContentType != TEXT) {
            generateDestinationUris(mediaDownloadInfo)
            Log.d(LOG_TAG, "Generated destination Uris")

            downloadMedia(mediaDownloadInfo).collect { downloadProgress ->
                flowCollector.emit(
                    R.string.processing_media_state_downloading_media,
                    40 + (0.6 * downloadProgress).roundToInt()
                )
            }
        }

        if (filteredMediaList.isNotEmpty())
            Log.d(LOG_TAG, "Downloaded media files, count: ${filteredMediaList.count()}")

        if (mediaDownloadInfo.mediaContentType == VIDEO_AUDIO) {
            combineVideoAndAudio(mediaDownloadInfo)
            flowCollector.emit(
                R.string.processing_media_state_downloading_media,
                100
            )
        }


        return mediaDownloadInfo
    }

    private fun combineVideoAndAudio(mediaDownloadInfo: MediaDownloadInfo) {
        try {
            val sw = Stopwatch().apply { start() }

            Log.d(LOG_TAG, "Starting to combine video and audio")

            val combinedUri = mediaPathProvider.getUriForMediaType(VIDEO_AUDIO)

            combineVideoAndAudio(
                mediaPathProvider.getFileForMediaType(VIDEO),
                mediaPathProvider.getFileForMediaType(AUDIO),
                fileIoService.openOutputStream(combinedUri) as FileOutputStream
            )

            Log.d(LOG_TAG, "Video and audio combined in ${sw.stopAndGetTimeElapsed()} ms.")
        } catch (e: Exception) {
            throw MediaVideoAudioCombineException(cause = e)
        }
    }

    private fun generateDestinationUris(filteredDownloadInfo: MediaDownloadInfo) {
        try {
            val isGallery = filteredDownloadInfo.mediaContentType == GALLERY
            filteredDownloadInfo.mediaDownloadList.forEach { media ->
                media.metadata.uri = mediaPathProvider.getUriForMediaType(
                    when {
                        isGallery -> GALLERY
                        else -> media.mediaType
                    },
                    media.galleryIndex
                )
            }
        } catch (e: Exception) {
            throw UriCreationException(cause = e)
        }
    }

    private suspend fun downloadMedia(
        filteredMediaInfo: MediaDownloadInfo
    ): Flow<Double> {
        try {
            return downloadService.downloadMedia(filteredMediaInfo.mediaDownloadList)
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
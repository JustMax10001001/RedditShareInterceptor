package com.justsoft.redditshareinterceptor

import android.net.Uri
import android.util.Log
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.ProcessingResult
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.model.media.MediaSpec
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
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

    fun handleUrl(url: String) {
        Log.d(LOG_TAG, "Starting url processing")
        try {
            startUrlProcessing(url)
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun startUrlProcessing(url: String) {
        val urlHandler = selectUrlHandler(url)
        FirebaseAnalyticsHelper.getInstance().logEvent("select_url_handler") {
            param("url", url)
            param("handler_name", urlHandler.javaClass.simpleName)
        }

        val unfilteredMediaList = urlHandler.processUrlAndGetMedia(url, requestHelper)
        Log.d(LOG_TAG, "Got unfiltered media, count: ${unfilteredMediaList.count()}")

        val filteredMediaList = filterMedia(unfilteredMediaList, MediaSpec())
        Log.d(LOG_TAG, "Filtered media, count: ${filteredMediaList.count()}")
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

    companion object {
        private const val LOG_TAG = "UUrlProcessor"
    }
}
package com.justsoft.redditshareinterceptor

import android.os.ParcelFileDescriptor
import com.google.firebase.analytics.ktx.logEvent
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.util.FirebaseAnalyticsHelper
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.websitehandlers.RedditUrlHandler
import com.justsoft.redditshareinterceptor.websitehandlers.UrlHandler

class UniversalUrlProcessor(
    private val requestHelper: RequestHelper,
    createDestinationFileDescriptor: (MediaContentType, Int) -> ParcelFileDescriptor
) {

    private val downloader = UniversalMediaDownloader(requestHelper, createDestinationFileDescriptor)

    private val websiteHandlers = listOf(
        RedditUrlHandler()
    )

    fun handleUrl(url: String) {
        val urlHandler = selectUrlHandler(url)
        FirebaseAnalyticsHelper.getInstance().logEvent("select_url_handler") {
            param("url", url)
            param("handler_name", urlHandler.javaClass.simpleName)
        }
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
}
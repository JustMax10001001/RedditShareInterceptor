package com.justsoft.redditshareinterceptor.websitehandlers

import com.justsoft.redditshareinterceptor.model.media.MediaDownloadInfo
import com.justsoft.redditshareinterceptor.util.request.RequestHelper

/**
 * Base interface for any website handler
 * Any website handler is expected to be stateless
 */
interface UrlHandler {

    fun isHandlerSuitableForUrl(url: String): Boolean

    /**
     * Does all the necessary processing
     * RequestHelper is provided for additional network requests
     * @return all possible downloads. The best option will be chosen and downloaded
     */
    fun processUrl(url: String, requestHelper: RequestHelper): MediaDownloadInfo
}
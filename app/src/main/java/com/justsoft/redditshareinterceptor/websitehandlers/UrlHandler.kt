package com.justsoft.redditshareinterceptor.websitehandlers

import com.justsoft.redditshareinterceptor.model.media.MediaList
import com.justsoft.redditshareinterceptor.util.RequestHelper

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
    fun processUrlAndGetMedia(url: String, requestHelper: RequestHelper): MediaList
}
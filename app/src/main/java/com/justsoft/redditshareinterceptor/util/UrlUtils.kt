package com.justsoft.redditshareinterceptor.util

import android.util.Patterns
import java.net.URLDecoder
import java.net.URLEncoder

fun urlEncode(url: String): String =
    URLEncoder.encode(url, "UTF-8")

fun urlDecode(encodedUrl: String): String =
    URLDecoder.decode(encodedUrl, "UTF-8")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&rt;", ">")

fun checkUrlString(url: String?): Boolean {
    url ?: return false
    return Patterns.WEB_URL.matcher(url).matches()
}
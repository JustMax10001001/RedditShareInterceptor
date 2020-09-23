package com.justsoft.redditshareinterceptor.util

import java.net.URLDecoder
import java.net.URLEncoder

fun urlEncode(url: String): String =
    URLEncoder.encode(url, "UTF-8")

fun urlDecode(encodedUrl: String): String =
    URLDecoder.decode(encodedUrl, "UTF-8")
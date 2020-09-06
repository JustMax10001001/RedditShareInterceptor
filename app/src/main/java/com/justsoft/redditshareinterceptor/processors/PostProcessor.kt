package com.justsoft.redditshareinterceptor.processors

import android.os.Bundle
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.model.RedditPost
import com.justsoft.redditshareinterceptor.util.RequestHelper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * An interface for processing different variations of posts
 * Provides default methods for making additional HTTP requests
 * Implementations of this interface are expected to be stateless
 */
interface PostProcessor {

    fun isProcessorSuitableForPost(redditPost: RedditPost): Boolean

    fun getPostContentType(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): ContentType

    fun getMediaDownloadUrl(redditPost: RedditPost, savedState: Bundle, requestHelper: RequestHelper): String
}
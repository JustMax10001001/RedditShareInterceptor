package com.justsoft.redditshareinterceptor.utils.request

import com.justsoft.redditshareinterceptor.utils.urlEncode
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Singleton

interface RequestHelper {

    fun readHttpTextResponse(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): String

    fun readHttpJsonResponse(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): JSONObject

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun openHttpConnection(url: String): HttpURLConnection = withContext(Dispatchers.IO) {
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty(
                "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"
            )
        }
    }

    fun getContentLength(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): Long = runBlocking { getContentLengthAsync(requestUrl, params).await() }


    suspend fun getContentLengthAsync(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): Deferred<Long> = GlobalScope.async(Dispatchers.IO) {
        val connection = openHttpConnection(requestUrl)

        if (params.isNotEmpty()) {
            connection.doOutput = true
            OutputStreamWriter(connection.outputStream).use {
                for ((key, value) in params)
                    it.write("${key}=${urlEncode(value)}")
            }
        }
        val length = connection.contentLengthLong
        connection.disconnect()

        length
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RequestHelperModule {
    @Binds
    @Singleton
    abstract fun bind(implementation: VolleyRequestHelper): RequestHelper
}
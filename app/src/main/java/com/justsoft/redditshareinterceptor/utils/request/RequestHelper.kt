package com.justsoft.redditshareinterceptor.utils.request

import com.justsoft.redditshareinterceptor.utils.urlEncode
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

interface RequestHelper {

    fun readHttpTextResponse(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): String

    fun readHttpJsonResponse(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): JSONObject

    fun getContentLength(
        requestUrl: String,
        params: MutableMap<String, String> = mutableMapOf()
    ): Long {
        val con: HttpURLConnection = URL(requestUrl).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)")

        if (params.isNotEmpty()) {
            con.doOutput = true
            OutputStreamWriter(con.outputStream).use {
                for ((key, value) in params)
                    it.write("${key}=${urlEncode(value)}")
            }
        }
        val length = con.contentLengthLong
        con.disconnect()
        return length
    }
}
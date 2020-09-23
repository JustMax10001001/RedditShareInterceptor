package com.justsoft.redditshareinterceptor.util

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Request helper that does not rely on Android Context - for easier testing
 */
class TestRequestHelper : RequestHelper {

    override fun readHttpTextResponse(
        requestUrl: String,
        params: MutableMap<String, String>
    ): String {
        val s = StringBuilder()
        val con: HttpURLConnection = URL(requestUrl).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

        if (params.isNotEmpty()) {
            con.doOutput = true
            OutputStreamWriter(con.outputStream).use {
                for ((key, value) in params)
                    it.write("${key}=${URLEncoder.encode(value, Charsets.UTF_8.displayName())}")
            }
        }

        val cbuf = CharArray(1024 * 4)
        val isr = BufferedReader(InputStreamReader(con.inputStream))
        var c = isr.read(cbuf)
        while (c > 0) {
            s.append(cbuf, 0, c)
            c = isr.read(cbuf)
        }
        con.disconnect()
        return s.toString()
    }

    override fun readHttpJsonResponse(
        requestUrl: String,
        params: MutableMap<String, String>
    ): JSONObject {
        return JSONObject(readHttpTextResponse(requestUrl, params))
    }

    override fun getContentLength(requestUrl: String, params: MutableMap<String, String>): Long {
        //TODO implement header parsing
        throw NotImplementedError()
    }
}
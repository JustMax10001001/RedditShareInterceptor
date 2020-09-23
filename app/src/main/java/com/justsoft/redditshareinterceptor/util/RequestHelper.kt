package com.justsoft.redditshareinterceptor.util

import android.os.ParcelFileDescriptor
import com.google.firebase.analytics.ktx.logEvent
import org.json.JSONObject
import java.io.FileOutputStream
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

    fun downloadFile(requestUrl: String, destinationFileDescriptor: ParcelFileDescriptor) {
        val sourceConnection: HttpURLConnection =
            URL(requestUrl).openConnection() as HttpURLConnection
        destinationFileDescriptor.use {
            FileOutputStream(destinationFileDescriptor.fileDescriptor).use { fileOutputStream ->
                val analytics = FirebaseAnalyticsHelper.getInstance()
                sourceConnection.inputStream.use { inputStream ->
                    val downloadStartTime = System.currentTimeMillis()
                    var downloadSize: Long = 0
                    val buffer = ByteArray(1024 * 1024)
                    var variableBufferSize: Int = 128 * 1024
                    var startTime = System.currentTimeMillis()
                    var bytesRead: Int
                    while (inputStream.read(buffer, 0, variableBufferSize)
                            .also { bytesRead = it } > 0
                    ) {
                        val delta = System.currentTimeMillis() - startTime
                        fileOutputStream.write(buffer, 0, bytesRead)

                        downloadSize += bytesRead

                        if (delta < 100)
                            variableBufferSize *= 2
                        if (delta > 500)
                            variableBufferSize /= 2
                        variableBufferSize = variableBufferSize
                            .coerceAtLeast(4 * 1024)
                            .coerceAtMost(1024 * 1024)
                        startTime = System.currentTimeMillis()
                    }

                    analytics.logEvent("media_downloaded") {
                        param("time_elapsed_ms", System.currentTimeMillis() - downloadStartTime)
                        param("download_size_bytes", downloadSize)
                        param("final_buffer_size", variableBufferSize.toLong())
                    }
                }
            }
        }
    }
}
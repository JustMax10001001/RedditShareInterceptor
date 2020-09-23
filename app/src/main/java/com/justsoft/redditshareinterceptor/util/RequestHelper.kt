package com.justsoft.redditshareinterceptor.util

import android.os.ParcelFileDescriptor
import org.json.JSONObject
import java.io.FileOutputStream
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
    ): Long

    fun downloadFile(requestUrl: String, destinationFileDescriptor: ParcelFileDescriptor) {
        val sourceConnection: HttpURLConnection =
            URL(requestUrl).openConnection() as HttpURLConnection
        destinationFileDescriptor.use {
            FileOutputStream(destinationFileDescriptor.fileDescriptor).use { fileOutputStream ->
                sourceConnection.inputStream.use { inputStream ->
                    val buffer = ByteArray(1024 * 1024)
                    var variableBufferSize = 128 * 1024
                    var startTime = System.currentTimeMillis()
                    var bytesRead: Int
                    while (inputStream.read(buffer, 0, variableBufferSize)
                            .also { bytesRead = it } > 0
                    ) {
                        val delta = System.currentTimeMillis() - startTime
                        fileOutputStream.write(buffer, 0, bytesRead)

                        if (delta < 100)
                            variableBufferSize *= 2
                        if (delta > 500)
                            variableBufferSize /= 2
                        variableBufferSize = variableBufferSize
                            .coerceAtLeast(4 * 1024)
                            .coerceAtMost(1024 * 1024)
                        startTime = System.currentTimeMillis()
                    }
                }
            }
        }
    }
}
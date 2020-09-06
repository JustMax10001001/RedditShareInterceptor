package com.justsoft.redditshareinterceptor.util

import org.json.JSONObject
import java.io.FileDescriptor
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

    fun downloadFile(requestUrl: String, destinationFileDescriptor: FileDescriptor) {
        val sourceConnection: HttpURLConnection = URL(requestUrl).openConnection() as HttpURLConnection
        val fileOutputStream = FileOutputStream(destinationFileDescriptor)
        val inputStream = sourceConnection.inputStream

        val buffer = ByteArray(1024 * 64)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } > 0) {
            fileOutputStream.write(buffer, 0, bytesRead)
        }
    }
}
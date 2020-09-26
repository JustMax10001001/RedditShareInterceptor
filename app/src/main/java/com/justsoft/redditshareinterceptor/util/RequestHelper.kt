package com.justsoft.redditshareinterceptor.util

import android.util.Log
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import org.json.JSONObject
import java.io.OutputStream
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

    fun downloadToOutputStream(
        requestUrl: String,
        outputStream: OutputStream,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        val sourceConnection: HttpURLConnection =
            URL(requestUrl).openConnection() as HttpURLConnection
        outputStream.use { fileOutputStream ->
            sourceConnection.inputStream.use { inputStream ->
                val downloadStartTime = System.currentTimeMillis()
                var downloadSize: Long = 0
                val buffer = ByteArray(16 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer)
                        .also { bytesRead = it } > 0
                ) {
                    fileOutputStream.write(buffer, 0, bytesRead)

                    downloadSize += bytesRead

                    downloadProgressCallback(
                        ProcessingProgress(
                            -1,
                            downloadSize.toInt()
                        )
                    )  // report how many bytes are already downloaded
                }

                val timeElapsed = System.currentTimeMillis() - downloadStartTime

                Log.d(
                    "RequestHelper",
                    ("Media of size %.2f MiB\r\n" +
                            "downloaded in $timeElapsed ms.\r\n" +
                            "with speed of %.2f MiB/s\r\n")
                        .format(
                            downloadSize / 1024.0 / 1024.0,
                            downloadSize / 1024.0 / 1024.0 / timeElapsed * 1000
                        )
                )
            }
        }
    }
}
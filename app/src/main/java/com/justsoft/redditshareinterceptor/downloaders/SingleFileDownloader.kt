package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import android.util.Log
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata
import com.justsoft.redditshareinterceptor.util.request.RequestHelper
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SingleFileDownloader(
    private val requestHelper: RequestHelper,
    private val outputStreamCallback: (Uri) -> OutputStream,
) {

    fun downloadFile(
        mediaObject: MediaDownloadObject,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        val outputStream = outputStreamCallback(mediaObject.metadata.uri)

        val sourceConnection = URL(mediaObject.downloadUrl).openConnection() as HttpURLConnection
        val totalSizeFuture = getTotalSize(mediaObject)
        var totalSize: Long = -1

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

                    if (totalSizeFuture.isDone && totalSize == (-1).toLong()) {
                        totalSize = totalSizeFuture.get()
                    }
                    if (totalSize != (-1).toLong()) {
                        downloadProgressCallback(
                            ProcessingProgress(
                                -1,
                                calculateProgress(downloadSize, totalSize)
                            )
                        )
                    }
                }

                val timeElapsed = System.currentTimeMillis() - downloadStartTime

                val downloadSizeMiB = downloadSize / 1024.0 / 1024.0
                val averageDownloadSpeed = downloadSizeMiB / timeElapsed * 1000

                Log.d(
                    "SingleFileDownloader",
                    ("Media of\r\n" +
                            "size %.2f MiB\r\n" +
                            "downloaded in $timeElapsed ms\r\n" +
                            "with speed of %.2f MiB/s\r\n")
                        .format(downloadSizeMiB, averageDownloadSpeed)
                )
            }
        }
    }

    private val additionalRequestExecutor = Executors.newSingleThreadExecutor()

    private fun getTotalSize(mediaObject: MediaDownloadObject): Future<Long> {
        return additionalRequestExecutor.submit<Long> {
            if (mediaObject.metadata.hasProperty(MediaMetadata.KEY_SIZE_BYTES))
                mediaObject.metadata.size
            else
                requestHelper.getContentLength(mediaObject.downloadUrl)
        }
    }

    private fun calculateProgress(downloaded: Long, totalSize: Long): Int =
        (downloaded.toDouble() / totalSize * 100).toInt()
}
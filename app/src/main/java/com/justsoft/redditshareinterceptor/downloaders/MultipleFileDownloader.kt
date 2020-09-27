package com.justsoft.redditshareinterceptor.downloaders

import android.net.Uri
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.util.RequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.OutputStream

class MultipleFileDownloader(
    private val requestHelper: RequestHelper,
    private val outputStreamCallback: (Uri) -> OutputStream,
) {

    fun downloadFiles(
        downloadObjects: MutableList<MediaDownloadObject>,
        downloadProgressCallback: (ProcessingProgress) -> Unit
    ) {
        val objectCount = downloadObjects.count()
        var previousProgress = 0
        var totalProgress = 0

        runBlocking(Dispatchers.IO) {
            downloadObjects.forEach { mediaObject ->
                launch {
                    val downloader = SingleFileDownloader(requestHelper, outputStreamCallback)

                    downloader.downloadFile(mediaObject) {
                        totalProgress += it.overallProgress
                        if (totalProgress - previousProgress >= objectCount) {
                            downloadProgressCallback(
                                ProcessingProgress(
                                    -1,
                                    totalProgress / objectCount
                                )
                            )
                            previousProgress = totalProgress
                        }
                    }
                }
            }
        }
    }
}
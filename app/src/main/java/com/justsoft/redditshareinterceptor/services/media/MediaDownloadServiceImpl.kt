package com.justsoft.redditshareinterceptor.services.media

import android.util.Log
import com.justsoft.redditshareinterceptor.R
import com.justsoft.redditshareinterceptor.model.ProcessingProgress
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata
import com.justsoft.redditshareinterceptor.model.send
import com.justsoft.redditshareinterceptor.services.io.FileIoService
import com.justsoft.redditshareinterceptor.utils.copyToStream
import com.justsoft.redditshareinterceptor.utils.format
import com.justsoft.redditshareinterceptor.utils.request.RequestHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

internal class MediaDownloadServiceImpl @Inject constructor(
    private val fileIoService: FileIoService,
    private val requestHelper: RequestHelper
) : MediaDownloadService {

    @ExperimentalCoroutinesApi
    override suspend fun downloadMedia(
        mediaList: List<MediaDownloadObject>
    ): Flow<ProcessingProgress> = channelFlow {
        withContext(Dispatchers.IO) {
            val mediaCount = mediaList.count()
            val progressArray = DoubleArray(mediaCount)

            var oldProgress = 0
            suspend fun checkAndEmitProgress() {
                val newProgress = (progressArray.sum() * 100 / mediaCount).roundToInt()

                if (newProgress - oldProgress > 0) {
                    send(R.string.processing_media_state_starting, oldProgress)
                    oldProgress = newProgress
                }
            }

            val downloadJobs = mediaList.mapIndexed { i, mediaObject ->
                launch {
                    downloadObject(mediaObject).collect { p ->
                        progressArray[i] = p

                        checkAndEmitProgress()
                    }
                }
            }

            downloadJobs.joinAll()
        }
    }

    private suspend fun downloadObject(mediaObject: MediaDownloadObject): Flow<Double> = flow {
        val totalDownloadSizeTask = getDownloadSizeBytesAsync(mediaObject)
        var totalDownloadSize: Long = -1
        var totalBytesDownloaded: Long = 0

        suspend fun incrementAndEmitProgress(readIncrement: Long) {
            totalBytesDownloaded += readIncrement

            if (totalDownloadSizeTask.isCompleted) {
                if (totalDownloadSize == (-1).toLong()) {
                    totalDownloadSize = totalDownloadSizeTask.await()
                }

                if (totalDownloadSize != (-1).toLong()) {
                    emit(totalBytesDownloaded.toDouble() / totalDownloadSize)
                }
            }
        }

        val sourceConnection = requestHelper.openHttpConnection(mediaObject.downloadUrl)
        val destinationStream = fileIoService.openOutputStream(mediaObject.metadata.uri)

        val downloadTimeMs = measureTimeMillis {

            sourceConnection.inputStream
                .copyToStream(destinationStream)
                .collect(::incrementAndEmitProgress)
        }

        printDownloadStats(downloadTimeMs, totalDownloadSize)
    }

    private fun printDownloadStats(downloadTimeMs: Long, totalDownloadSize: Long) {
        val downloadSizeMiB = totalDownloadSize / 1024.0 / 1024.0
        val averageDownloadSpeed = downloadSizeMiB / downloadTimeMs * 1000

        Log.d(
            "MediaDownloadServiceImpl",
            "Media of\r\n" +
                    "size ${downloadSizeMiB.format(2)} MiB\r\n" +
                    "downloaded in $downloadTimeMs ms\r\n" +
                    "with speed of ${averageDownloadSpeed.format(2)} MiB/s\r\n"
        )
    }

    private suspend fun getDownloadSizeBytesAsync(mediaObject: MediaDownloadObject): Deferred<Long> {
        if (mediaObject.metadata.hasProperty(MediaMetadata.KEY_SIZE_BYTES))
            return CompletableDeferred(mediaObject.metadata.size)

        return requestHelper.getContentLengthAsync(mediaObject.downloadUrl)
    }
}
package com.justsoft.redditshareinterceptor.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

private const val DEFAULT_BUFFER_SIZE = 16 * 1024

/**
 * Reads the InputStream and writes to the OutputStream
 * @return - flow of the count of bytes read from the InputStream
 */
@Suppress("BlockingMethodInNonBlockingContext")
suspend fun InputStream.copyToStream(
    toStream: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
): Flow<Long> = flow {
    this@copyToStream.use { fromStream ->
        toStream.use { _ ->
            withContext(Dispatchers.IO) {
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int

                while (fromStream.read(buffer).also { bytesRead = it } > 0) {
                    toStream.write(buffer, 0, bytesRead)

                    emit(bytesRead.toLong())
                }
            }
        }
    }
}
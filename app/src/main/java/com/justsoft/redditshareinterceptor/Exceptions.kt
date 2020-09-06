package com.justsoft.redditshareinterceptor

import com.justsoft.redditshareinterceptor.processors.PostProcessor

open class ProcessorSelectionException(
    message: String = "",
    cause: Throwable? = null
) : Exception(message, cause)

class MultipleSuitableProcessorsExceptions(
    message: String = "There are multiple suitable PostProcessors.",
    cause: Throwable? = null,
    processors: Iterable<PostProcessor>
) : ProcessorSelectionException(message, cause) {

    private val localMessage: String

    override val message: String?
        get() = super.message + localMessage

    init {
        val sb = StringBuilder()
            .append("Available processors are: ")
        val it = processors.iterator()
        while (it.hasNext()) {
            sb.append(it.next().javaClass.canonicalName)
            if (it.hasNext())
                sb.append(", ")
        }
        localMessage = sb.toString()
    }
}

class NoSuitableProcessorException(
    message: String = "There are no suitable PostProcessors."
): ProcessorSelectionException(message)

open class MediaDownloadException(
    message: String = "Error while downloading post media.",
    cause: Throwable? = null
): Exception(message, cause)

class DescriptorCreationException(
    message: String = "Unable to create file descriptor.",
    cause: Throwable? = null
): MediaDownloadException(message, cause)

open class PostProcessingException(
    message: String = "Unable to process post.",
    cause: Throwable? = null
): Exception(message, cause)

class PostContentTypeAcquiringException(
    message: String = "Unable to get post content type.",
    cause: Throwable? = null
): PostProcessingException(message, cause)

class PostContentUrlAcquiringException(
    message: String = "Unable to get post content url.",
    cause: Throwable? = null
): PostProcessingException(message, cause)
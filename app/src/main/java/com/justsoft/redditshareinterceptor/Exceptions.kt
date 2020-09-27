package com.justsoft.redditshareinterceptor

import com.justsoft.redditshareinterceptor.processors.PostProcessor
import com.justsoft.redditshareinterceptor.websitehandlers.UrlHandler

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



open class UrlHandlerSelectionException(
    message: String = "",
    cause: Throwable? = null
) : Exception(message, cause)

class MultipleSuitableUrlHandlersExceptions(
    message: String = "There are multiple suitable UrlHandlers.",
    cause: Throwable? = null,
    handlers: Iterable<UrlHandler>
) : UrlHandlerSelectionException(message, cause) {

    private val localMessage: String

    override val message: String?
        get() = super.message + localMessage

    init {
        val sb = StringBuilder()
            .append("Available handlers are: ")
        val it = handlers.iterator()
        while (it.hasNext()) {
            sb.append(it.next().javaClass.canonicalName)
            if (it.hasNext())
                sb.append(", ")
        }
        localMessage = sb.toString()
    }
}

class NoSuitableUrlHandlerException(
    message: String = ""
): UrlHandlerSelectionException("There are no suitable UrlHandlers. Maybe Url is wrong or malformed. $message")



open class MediaDownloadException(
    message: String = "Error while downloading post media.",
    cause: Throwable? = null
): Exception(message, cause)

class UriCreationException(
    message: String = "Unable to get Uri for media.",
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

class MediaFilterException(
    message: String = "Unable to filter through media available for download",
    cause: Throwable? = null
): PostProcessingException(message, cause)
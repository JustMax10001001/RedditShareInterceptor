package com.justsoft.redditshareinterceptor.util

import android.util.Xml
import com.justsoft.redditshareinterceptor.model.media.MediaContentType
import com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject
import com.justsoft.redditshareinterceptor.model.media.metadata.MediaMetadata
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

/**
 * A class to parse Reddit MPDs into list of
 * [MediaDownloadObjects][com.justsoft.redditshareinterceptor.model.media.MediaDownloadObject]
 *
 * @param baseUrl - base url for downloading media objects
 */
class MPDParser(
    private var baseUrl: String
) {
    init {
        if (!baseUrl.endsWith('/'))
            baseUrl += '/'
    }

    lateinit var contentType: MediaContentType
        private set

    public fun parse(inputStream: InputStream): List<MediaDownloadObject> {
        inputStream.use {
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(inputStream, Charsets.UTF_8.displayName())
                nextTag()
            }

            return readList(parser)
        }
    }

    private fun readList(parser: XmlPullParser): List<MediaDownloadObject> {
        val mediaObjects = mutableListOf<MediaDownloadObject>()

        var adaptationSetCount = 0
        parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_MPD)

        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == TAG_MPD)) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                // skip this event
                continue
            }

            if (parser.name == TAG_ADAPTATION_SET) {
                ++adaptationSetCount
                //mediaObjects.add(parseMediaObject(parser))
                mediaObjects.addAll(parseAdaptationSet(parser))
            } else {
                continue
            }
        }

        parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_MPD)

        contentType = when (adaptationSetCount) {
            1 -> MediaContentType.VIDEO
            2 -> MediaContentType.VIDEO_AUDIO
            else -> throw IllegalStateException("Found $adaptationSetCount AdaptationSet tags while expecting 1 or 2")
        }

        return mediaObjects
    }

    private fun parseAdaptationSet(parser: XmlPullParser): List<MediaDownloadObject> {
        val mediaObjects = mutableListOf<MediaDownloadObject>()

        parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_ADAPTATION_SET)

        val contentType = getContentType(parser)

        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == TAG_ADAPTATION_SET)) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                // skip this event
                continue
            }

            if (parser.name == TAG_REPRESENTATION) {
                mediaObjects.add(parseMediaObject(parser, contentType))
            } else {
                continue
            }
        }

        parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_ADAPTATION_SET)

        return mediaObjects
    }

    private fun parseMediaObject(parser: XmlPullParser, contentType: MediaContentType): MediaDownloadObject {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_REPRESENTATION)
        val bitrate = getBitrate(parser)

        var url = ""
        while (!(parser.nextTag() == XmlPullParser.END_TAG && parser.name == TAG_REPRESENTATION)) {
            if (parser.name == TAG_BASE_URL) {
                url = getMediaURL(parser)
            } else {
                continue
            }
        }

        parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_REPRESENTATION)

        return MediaDownloadObject(
            url,
            contentType,
            metadata = MediaMetadata().apply { this.bitrate = bitrate }
        )
    }

    private fun getMediaURL(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, NAMESPACE, TAG_BASE_URL)

        val url = baseUrl + readText(parser)

        parser.require(XmlPullParser.END_TAG, NAMESPACE, TAG_BASE_URL)
        return url
    }

    private fun readText(parser: XmlPullParser): String {
        var value = ""
        if (parser.next() == XmlPullParser.TEXT) {
            value = parser.text
            parser.nextTag()
        }
        return value
    }

    private fun getBitrate(parser: XmlPullParser) =
        parser.getAttribute(ATTRIBUTE_BANDWIDTH)?.toInt() ?: 0

    private fun getContentType(parser: XmlPullParser) =
        when (parser.getAttribute(ATTRIBUTE_CONTENT_TYPE)) {
            VALUE_AUDIO -> MediaContentType.AUDIO
            else -> MediaContentType.VIDEO
        }


    private fun skipToTagEnd(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException("Parser has not hit the start of tag!")
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun XmlPullParser.getAttribute(attribute: String): String? {
        return getAttributeValue(null, attribute)
    }

    companion object {
        private val NAMESPACE: String? = null

        private const val TAG_MPD = "MPD"
        private const val TAG_ADAPTATION_SET = "AdaptationSet"
        private const val TAG_REPRESENTATION = "Representation"
        private const val TAG_BASE_URL = "BaseURL"

        private const val ATTRIBUTE_CONTENT_TYPE = "contentType"
        private const val ATTRIBUTE_BANDWIDTH = "bandwidth"

        private const val VALUE_AUDIO = "audio"
        private const val VALUE_VIDEO = "video"
    }
}
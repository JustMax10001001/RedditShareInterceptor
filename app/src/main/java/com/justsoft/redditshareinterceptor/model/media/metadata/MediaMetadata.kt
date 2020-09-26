package com.justsoft.redditshareinterceptor.model.media.metadata

import android.net.Uri

class MediaMetadata {

    private val propertyMap = mutableMapOf<String, Any>()

    var resolutionX: Int
        get() = tryGet(KEY_RESOLUTION_X)
        set(value) {
            propertyMap[KEY_RESOLUTION_X] = value
        }

    var resolutionY: Int
        get() = tryGet(KEY_RESOLUTION_Y)
        set(value) {
            propertyMap[KEY_RESOLUTION_Y] = value
        }

    var duration: Int
        get() = tryGet(KEY_DURATION_SECONDS)
        set(value) {
            propertyMap[KEY_DURATION_SECONDS] = value
        }

    var bitrate: Int
        get() = tryGet(KEY_BITRATE)
        set(value) {
            propertyMap[KEY_BITRATE] = value
        }

    var size: Long
        get() = tryGet(KEY_SIZE_BYTES)
        set(value) {
            propertyMap[KEY_SIZE_BYTES] = value
        }

    var uri: Uri
        get() = tryGet(KEY_LOCATION_URI)
        set(value) {
            propertyMap[KEY_LOCATION_URI] = value
        }

    fun hasProperty(key: String): Boolean = propertyMap.containsKey(key)

    private fun <T> tryGet(key: String): T {
        if (!propertyMap.containsKey(key))
            throw IllegalStateException("Property $key is not set!")
        @Suppress("UNCHECKED_CAST")
        return try {
            propertyMap[key]!! as T
        } catch (e: Exception) {
            throw IllegalStateException("Property $key has wrong type (present: ${propertyMap[key]?.javaClass?.simpleName})!")
        }
    }

    companion object {
        const val KEY_RESOLUTION_X = "resolution_x"
        const val KEY_RESOLUTION_Y = "resolution_y"

        const val KEY_BITRATE = "bitrate"
        const val KEY_DURATION_SECONDS = "duration"
        const val KEY_SIZE_BYTES = "size"

        const val KEY_LOCATION_URI = "uri"
    }

    class Builder() {

        private val metadata = MediaMetadata()

        //var resolutionX: Int
        //set(value) = metadata.propertyMap[KEY_RESOLUTION_X] =
    }
}

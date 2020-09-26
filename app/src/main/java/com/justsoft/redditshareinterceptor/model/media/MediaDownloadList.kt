package com.justsoft.redditshareinterceptor.model.media

class MediaDownloadList(val listMediaContentType: MediaContentType, var caption: String = "") :
    ArrayList<MediaDownloadObject>() {

    private fun processSubList(mediaSpec: MediaSpec, sortedList: List<MediaDownloadObject>): MediaDownloadObject {
        val threshold = mediaSpec.getThresholdForType(listMediaContentType)
        val iterator = sortedList.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.size <= threshold)
                return file
        }
        return sortedList.last()
    }

    fun getMostSuitableMedia(mediaSpec: MediaSpec = MediaSpec()): MediaDownloadList {
        if (this.isEmpty())
            throw IllegalStateException("MediaList is empty!")

        if (this.count() == 1)
            return mediaDownloadListOf(this)

        val sortedList = this.sortedWith(compareBy(MediaDownloadObject::galleryIndex, { -it.size }))
        return MediaDownloadList(listMediaContentType, caption).apply {
            addAll(
                sortedList
                    .groupBy(MediaDownloadObject::galleryIndex)
                    .map { processSubList(mediaSpec, it.value) }
            )
        }
    }
}

fun mediaDownloadListOf(mediaType: MediaContentType, caption: String = ""): MediaDownloadList =
    MediaDownloadList(mediaType, caption)

fun mediaDownloadListOf(contentType: MediaContentType): MediaDownloadList = mediaDownloadListOf(contentType, "")

fun mediaDownloadListOf(caption: String, vararg media: MediaDownloadObject): MediaDownloadList =
    mediaDownloadListOf(media[0].mediaType, caption).apply { this.addAll(media) }

fun mediaDownloadListOf(mediaList: MediaDownloadList): MediaDownloadList =
    MediaDownloadList(mediaList.listMediaContentType, mediaList.caption).apply { this.addAll(mediaList) }

fun mediaDownloadListOf(vararg media: MediaDownloadObject): MediaDownloadList =
    mediaDownloadListOf(media[0].mediaType).apply { this.addAll(media) }
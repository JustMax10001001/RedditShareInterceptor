package com.justsoft.redditshareinterceptor.model.media

class MediaList(val listMediaContentType: MediaContentType, var caption: String = "") :
    ArrayList<MediaModel>() {

    private fun processSubList(mediaSpec: MediaSpec, sortedList: List<MediaModel>): MediaModel {
        val threshold = mediaSpec.getThresholdForType(listMediaContentType)
        val iterator = sortedList.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.size <= threshold)
                return file
        }
        return sortedList.last()
    }

    fun getMostSuitableMedia(mediaSpec: MediaSpec = MediaSpec()): MediaList {
        if (this.isEmpty())
            throw IllegalStateException("MediaList is empty!")

        if (this.count() == 1)
            return mediaListOf(this)

        val sortedList = this.sortedWith(compareBy(MediaModel::index, { -it.size }))
        return MediaList(listMediaContentType, caption).apply {
            addAll(
                sortedList
                    .groupBy(MediaModel::index)
                    .map { processSubList(mediaSpec, it.value) }
            )
        }
    }
}

fun mediaListOf(mediaType: MediaContentType, caption: String = ""): MediaList =
    MediaList(mediaType, caption)

fun mediaListOf(contentType: MediaContentType): MediaList = mediaListOf(contentType, "")

fun mediaListOf(caption: String, vararg media: MediaModel): MediaList =
    mediaListOf(media[0].mediaType, caption).apply { this.addAll(media) }

fun mediaListOf(mediaList: MediaList): MediaList =
    MediaList(mediaList.listMediaContentType, mediaList.caption).apply { this.addAll(mediaList) }

fun mediaListOf(vararg media: MediaModel): MediaList =
    mediaListOf(media[0].mediaType).apply { this.addAll(media) }
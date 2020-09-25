package com.justsoft.redditshareinterceptor.model.media

class MediaList(val listMediaContentType: MediaContentType, var caption: String = "") : ArrayList<MediaModel>() {

    /*private fun checkElementContentType(element: MediaModel) {
        if (element.mediaType != listMediaContentType)
            throw IllegalArgumentException(
                "Can't add element to list because it has different type: " +
                        "List has type $listMediaContentType while the element has typ ${element.mediaType}"
            )
    }

    override fun replaceAll(operator: UnaryOperator<MediaModel>) {
        super.replaceAll(operator)
        this.forEach(::checkElementContentType)
    }

    override fun set(index: Int, element: MediaModel): MediaModel {
        checkElementContentType(element)
        return super.set(index, element)
    }

    override fun addAll(index: Int, elements: Collection<MediaModel>): Boolean {
        elements.forEach(::checkElementContentType)
        return super.addAll(index, elements)
    }

    override fun addAll(elements: Collection<MediaModel>): Boolean {
        elements.forEach(::checkElementContentType)
        return super.addAll(elements)
    }

    override fun add(index: Int, element: MediaModel) {
        checkElementContentType(element)
        super.add(index, element)
    }

    override fun add(element: MediaModel): Boolean {
        checkElementContentType(element)
        return super.add(element)
    }*/

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
            return mediaListOf(this.listMediaContentType, caption).also { addAll(this) }

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

fun mediaListOf(mediaType: MediaContentType, caption: String = ""): MediaList
    = MediaList(mediaType, caption)

fun mediaListOf(contentType: MediaContentType): MediaList = mediaListOf(contentType, "")

fun mediaListOf(caption: String, vararg media: MediaModel): MediaList =
    mediaListOf(media[0].mediaType, caption).apply { this.addAll(media) }

fun mediaListOf(vararg media: MediaModel): MediaList =
    mediaListOf(media[0].mediaType).apply { this.addAll(media) }
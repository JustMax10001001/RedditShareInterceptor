package com.justsoft.redditshareinterceptor.model.media

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.function.UnaryOperator

class MediaList(private val listMediaContentType: MediaContentType) : ArrayList<MediaModel>() {

    private fun checkElementContentType(element: MediaModel) {
        if (element.mediaType != listMediaContentType)
            throw IllegalArgumentException(
                "Can't add element to list because it has different type: " +
                        "List has type $listMediaContentType while the element has typ ${element.mediaType}"
            )
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
    }

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
        val sortedList = this.sortedWith(compareBy(MediaModel::index, { -it.size }))
        return MediaList(listMediaContentType).apply {
            addAll(
                sortedList
                    .groupBy(MediaModel::index)
                    .map { processSubList(mediaSpec, it.value) }
            )
        }
    }
}

fun mediaListOf(contentType: MediaContentType): MediaList = MediaList(contentType)

fun mediaListOf(vararg media: MediaModel): MediaList =
    MediaList(media[0].mediaType).apply { this.addAll(media) }
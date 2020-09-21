package com.justsoft.redditshareinterceptor.util

import com.justsoft.redditshareinterceptor.model.MediaModel

class MediaFileChooser {

    companion object {

        fun getBestMediaFile(mediaFiles: Collection<MediaModel>): MediaModel {
            val sorted = mediaFiles.sortedByDescending(MediaModel::size)
            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val file = iterator.next()
                if (file.size <= VIDEO_SIZE_THRESHOLD)
                    return file
            }
            return sorted.last()
        }

        private const val VIDEO_SIZE_THRESHOLD = 10 * 1024 * 1024       // 10 MiB
    }
}
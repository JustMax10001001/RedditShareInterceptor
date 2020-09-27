package com.justsoft.redditshareinterceptor.model.media

class MediaDownloadInfo(
    var mediaContentType: MediaContentType,
    var caption: String = "",
    srcMediaObjectList: List<MediaDownloadObject> = emptyList()
) {
    val mediaDownloadList: MutableList<MediaDownloadObject> = mutableListOf()

    init {
        mediaDownloadList.addAll(srcMediaObjectList)
    }
}
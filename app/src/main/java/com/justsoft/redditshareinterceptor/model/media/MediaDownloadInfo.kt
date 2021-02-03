package com.justsoft.redditshareinterceptor.model.media

class MediaDownloadInfo(
    val mediaContentType: MediaContentType,
    val caption: String,
    val requestUrl: String,
    srcMediaObjectList: List<MediaDownloadObject> = emptyList(),
    val isVideoAudioPair: Boolean = false
) {
    val mediaDownloadList: MutableList<MediaDownloadObject> = mutableListOf()

    init {
        mediaDownloadList.addAll(srcMediaObjectList)
    }
}
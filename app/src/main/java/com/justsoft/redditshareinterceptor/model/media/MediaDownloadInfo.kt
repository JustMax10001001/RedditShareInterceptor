package com.justsoft.redditshareinterceptor.model.media

class MediaDownloadInfo(
    var mediaContentType: MediaContentType,
    var caption: String = "",
    val mediaDownloadList: MutableList<MediaDownloadObject> = mutableListOf()
)

/*fun mediaDownloadListOf(mediaType: MediaContentType, caption: String = ""): MediaDownloadInfo =
    MediaDownloadInfo(mediaType, caption)

fun mediaDownloadListOf(contentType: MediaContentType): MediaDownloadInfo =
    mediaDownloadListOf(contentType, "")

fun mediaDownloadListOf(caption: String, vararg media: MediaDownloadObject): MediaDownloadInfo =
    mediaDownloadListOf(media[0].mediaType, caption).apply { this.addAll(media) }

fun mediaDownloadListOf(mediaInfo: MediaDownloadInfo): MediaDownloadInfo =
    MediaDownloadInfo(mediaInfo.mediaContentType, mediaInfo.caption).apply {
        this.addAll(
            mediaInfo
        )
    }

fun mediaDownloadListOf(vararg media: MediaDownloadObject): MediaDownloadInfo =
    mediaDownloadListOf(media[0].mediaType).apply { this.addAll(media) }*/
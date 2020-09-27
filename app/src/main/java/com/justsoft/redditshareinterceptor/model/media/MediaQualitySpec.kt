package com.justsoft.redditshareinterceptor.model.media


class MediaQualitySpec private constructor() {
    var videoSizeX: Int = -1
        private set
    var videoSizeY: Int = -1
        private set
    var videoBitrate: Int = -1
        private set
    var videoFileSize: Long = -1
        private set

    var imageSizeX: Int = -1
        private set
    var imageSizeY: Int = -1
        private set
    var imageFileSize: Int = -1
        private set



    companion object {
        val PRESET_HIGH = MediaQualitySpec().apply {
            videoSizeX = 1280
            videoSizeY = 720
            videoBitrate = 3145728          // 7.5 MiB in 20 s
            videoFileSize = (7.5 * 1024 * 1024).toLong()

            imageSizeX = 1024
            imageSizeY = 1024
            imageFileSize = 512 * 1024      // 512 KiB
        }

        val PRESET_MEDIUM = MediaQualitySpec().apply {
            videoSizeX = 1024
            videoSizeY = 576
            videoBitrate = 2097152          // 5 MiB in 20 s
            videoFileSize = (5 * 1024 * 1024).toLong()

            imageSizeX = 768
            imageSizeY = 768
            imageFileSize = 256 * 1024      // 512 KiB
        }

        val PRESET_LOW = MediaQualitySpec().apply {
            videoSizeX = 720
            videoSizeY = 405
            videoBitrate = 838860           // 2 MiB in 20 s
            videoFileSize = (2 * 1024 * 1024).toLong()

            imageSizeX = 512
            imageSizeY = 512
            imageFileSize = 96 * 1024      // 512 KiB
        }
    }
}
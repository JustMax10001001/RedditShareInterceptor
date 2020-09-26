package com.justsoft.redditshareinterceptor.model.media

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

class MediaDownloadListTest {

    private fun mockVideoMedia(size: Long) =
        MediaDownloadObject("mock://vid.eo", MediaContentType.VIDEO)
            .apply { metadata.size = size }

    private fun mockImagesForGalleryIndex(
        index: Int,
        sizes: Sequence<Long>
    ): Sequence<MediaDownloadObject> =
        sizes.map {
            MediaDownloadObject("mock://galle.ry", MediaContentType.GALLERY, index)
                .apply { metadata.size = it }
        }

    private fun mockGallery(): Sequence<MediaDownloadObject> {
        val sizeSequence = generateSequence(6.toLong()) { it + 5 }
        val idSequence = generateSequence(1) { it + 1 }
        return idSequence.flatMap { mockImagesForGalleryIndex(it, sizeSequence).take(5) }
    }

    @Test
    fun getMostSuitableMedia_TestVideo_NormalConditions() {
        val mediaList = MediaDownloadList(MediaContentType.VIDEO)
        mediaList.addAll(
            listOf(
                mockVideoMedia(1),
                mockVideoMedia(2),
                mockVideoMedia(3)
            )
        )
        val mockSpec = Mockito.mock(MediaQualitySpec::class.java)
        Mockito.`when`(mockSpec.videoFileSize).thenReturn(2)
        assertEquals(2, mediaList.getMostSuitableMedia(mockSpec)[0].metadata.size)
    }

    @Test
    fun getMostSuitableMedia_TestVideo_ThresholdBiggerThenMaxVideo() {
        val mediaList = MediaDownloadList(MediaContentType.VIDEO)
        mediaList.addAll(
            listOf(
                mockVideoMedia(1),
                mockVideoMedia(2),
                mockVideoMedia(3)
            )
        )
        val mockSpec = Mockito.mock(MediaQualitySpec::class.java)
        Mockito.`when`(mockSpec.videoFileSize).thenReturn(4)
        assertEquals(3, mediaList.getMostSuitableMedia(mockSpec)[0].metadata.size)
    }

    @Test
    fun getMostSuitableMedia_TestVideo_ThresholdLowerThenMinVideo() {
        val mediaList = MediaDownloadList(MediaContentType.VIDEO)
        mediaList.addAll(
            listOf(
                mockVideoMedia(4),
                mockVideoMedia(5),
                mockVideoMedia(3)
            )
        )
        val mockSpec = Mockito.mock(MediaQualitySpec::class.java)
        Mockito.`when`(mockSpec.videoFileSize).thenReturn(2)
        assertEquals(3, mediaList.getMostSuitableMedia(mockSpec)[0].metadata.size)
    }

    @Test
    fun getMostSuitableMedia_TestGallery_NormalConditions() {
        val mockSpec = Mockito.mock(MediaQualitySpec::class.java)
        Mockito.`when`(mockSpec.imageFileSize).thenReturn(15)
        val mediaList = MediaDownloadList(MediaContentType.GALLERY)
        mediaList.addAll(mockGallery().take(25).toList())
        mediaList.getMostSuitableMedia(mockSpec).forEach {
            assertEquals(16, it.metadata.size)
        }
    }

    @Test
    fun getMostSuitableMedia_TestGallery_ThresholdBiggerThenMinImage() {
        val mockSpec = Mockito.mock(MediaQualitySpec::class.java)
        Mockito.`when`(mockSpec.imageFileSize).thenReturn(4)
        val mediaList = MediaDownloadList(MediaContentType.GALLERY)
        mediaList.addAll(mockGallery().take(25).toList())
        mediaList.getMostSuitableMedia(mockSpec).forEach {
            assertEquals(6, it.metadata.size)
        }
    }
}
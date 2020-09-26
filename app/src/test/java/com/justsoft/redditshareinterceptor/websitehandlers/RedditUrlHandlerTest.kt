package com.justsoft.redditshareinterceptor.websitehandlers

import com.justsoft.redditshareinterceptor.NoSuitableProcessorException
import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.TestRequestHelper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class RedditUrlHandlerTest {

    private val requestHelper = TestRequestHelper()
    private val redditPostHandler: RedditUrlHandler = RedditUrlHandler()

    @Test
    fun testPostObjectDownload() {
        val post =
            redditPostHandler.downloadRedditPost(
                "https://www.reddit.com/by_id/t3_ilt7jo/",
                requestHelper
            )
        assertEquals(post.title, "Let's not forget the original meme mod")
    }

    @Test(expected = NoSuitableProcessorException::class)
    fun testPostControllerSelector_NoController() {
        val requestUrl = "https://www.reddit.com/r/feedthememes/comments/ilt7jo/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("https://www.reddit.com/by_id/t3_ilt7jo/.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject()
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        postHandler.selectPostProcessor(post)
    }

    @Test
    fun testPostControllerSelector_RedditImage() {
        val requestUrl = "https://www.reddit.com/r/techsupportgore/comments/ilrwy8/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("https://www.reddit.com/by_id/t3_ilrwy8/.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://i.redd.it/mock.jpg"
                        )
                    )
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        assert(postHandler.selectPostProcessor(post) is RedditImagePostProcessor)
    }

    @Test
    fun testPostControllerSelector_RedditGallery() {
        val requestUrl = "https://www.reddit.com/r/announcements/comments/hrrh23/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse(anyString(), any()))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://www.reddit.com/gallery/hrrh23"
                        )
                    )
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        assert(postHandler.selectPostProcessor(post) is RedditGalleryPostProcessor)
    }

    @Test
    fun testPostControllerSelector_ImgurImage() {
        val requestUrl = "https://www.reddit.com/r/feedthememes/comments/ilt7jo/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("https://www.reddit.com/by_id/t3_ilt7jo/.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://i.imgur.com/mock.jpg"
                        )
                    )
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        assert(postHandler.selectPostProcessor(post) is RedditImagePostProcessor)
    }

    @Test
    fun testPostControllerSelector_RedditVideo() {
        val requestUrl = "https://www.reddit.com/r/Unexpected/comments/ilrghf/\""
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("https://www.reddit.com/by_id/t3_ilrghf/.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://v.redd.it/mock.mp4"
                        )
                    )
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        assert(postHandler.selectPostProcessor(post) is RedditVideoPostProcessor)
    }

    @Test
    fun testPostControllerSelector_Gfycat() {
        val requestUrl = "https://www.reddit.com/r/Eyebleach/comments/gzn5on/\""
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("https://www.reddit.com/by_id/t3_gzn5on/.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://gfycat.com/somefancymocks.mp4"
                        )
                    )
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        assert(postHandler.selectPostProcessor(post) is GfycatPostProcessor)
    }

    @Test
    fun testPostControllerSelector_RedGifs() {
        val requestUrl = "https://www.reddit.com/r/Eyebleach/comments/gzn5on/\""
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("https://www.reddit.com/by_id/t3_gzn5on/.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://redgifs.com/somefancymocks.mp4"
                        )
                    )
                )
            )
        val postHandler = RedditUrlHandler()
        val post = postHandler.downloadRedditPost(requestUrl, mockRequestHelper)
        assert(postHandler.selectPostProcessor(post) is RedGifsPostProcessor)
    }

    private fun createMockPostObject(overrides: Map<String, Any?> = emptyMap()): JSONObject {
        val obj =
            JSONObject("{\"data\": {\"selftext\": \"\",\"title\": \"Mock title\",\"subreddit_name_prefixed\": \"r/mock\",\"url\": \"https://mock.com/mock\"}}")
        for ((key, value) in overrides) {
            obj.getJSONObject("data")
                .put(key, value)
        }
        return obj
    }

    private fun createMockPostJsonResponse(post: JSONObject): String {
        val arr = JSONObject("{\"data\": {\"children\": []}}")
        arr.getJSONObject("data")
            .getJSONArray("children")
            .put(post)
        return arr.toString()
    }
}

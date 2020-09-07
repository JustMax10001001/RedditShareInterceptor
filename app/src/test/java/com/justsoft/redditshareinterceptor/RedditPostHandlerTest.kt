package com.justsoft.redditshareinterceptor

import com.justsoft.redditshareinterceptor.processors.*
import com.justsoft.redditshareinterceptor.util.RequestHelper
import com.justsoft.redditshareinterceptor.util.TestRequestHelper
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

class RedditPostHandlerTest {

    private val redditPostHandler: RedditPostHandler = RedditPostHandler(TestRequestHelper())

    @Test
    fun extractSimpleUrl() {
        val testUrl =
            "https://www.reddit.com/r/nextfuckinglevel/comments/iloqpd/beautiful_horse_mural/"
        val expected = "https://www.reddit.com/r/nextfuckinglevel/comments/iloqpd/"
        assertEquals(expected, redditPostHandler.extractSimpleUrl(testUrl))
    }

    @Test
    fun testPostObjectDownload() {
        val post =
            redditPostHandler.getRedditPostObj("https://www.reddit.com/r/feedthememes/comments/ilt7jo/")
        assertEquals(post.title, "Let's not forget the original meme mod")
    }

    @Test
    fun testPostControllerSelector_RedditImage() {
        val requestUrl = "https://www.reddit.com/r/techsupportgore/comments/ilrwy8/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("$requestUrl.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://i.redd.it/mock.jpg"
                        )
                    )
                )
            )
        val postHandler = RedditPostHandler(mockRequestHelper)
        val post = postHandler.getRedditPostObj(requestUrl)
        assert(postHandler.selectPostProcessor(post) is RedditImagePostProcessor)
    }

    @Test(expected = NoSuitableProcessorException::class)
    fun testPostControllerSelector_NoController() {
        val requestUrl = "https://www.reddit.com/r/feedthememes/comments/ilt7jo/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("$requestUrl.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject()
                )
            )
        val postHandler = RedditPostHandler(mockRequestHelper)
        val post = postHandler.getRedditPostObj(requestUrl)
        postHandler.selectPostProcessor(post)
    }

    @Test
    fun testPostControllerSelector_ImgurImage() {
        val requestUrl = "https://www.reddit.com/r/feedthememes/comments/ilt7jo/"
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("$requestUrl.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://i.imgur.com/mock.jpg"
                        )
                    )
                )
            )
        val postHandler = RedditPostHandler(mockRequestHelper)
        val post = postHandler.getRedditPostObj(requestUrl)
        assert(postHandler.selectPostProcessor(post) is ImgurImageProcessor)
    }

    @Test
    fun testPostControllerSelector_RedditVideo() {
        val requestUrl = "https://www.reddit.com/r/Unexpected/comments/ilrghf/\""
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("$requestUrl.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://v.redd.it/mock.mp4"
                        )
                    )
                )
            )
        val postHandler = RedditPostHandler(mockRequestHelper)
        val post = postHandler.getRedditPostObj(requestUrl)
        assert(postHandler.selectPostProcessor(post) is RedditVideoPostProcessor)
    }

    @Test
    fun testPostControllerSelector_Gfycat() {
        val requestUrl = "https://www.reddit.com/r/Eyebleach/comments/gzn5on/\""
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("$requestUrl.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://gfycat.com/somefancymocks.mp4"
                        )
                    )
                )
            )
        val postHandler = RedditPostHandler(mockRequestHelper)
        val post = postHandler.getRedditPostObj(requestUrl)
        assert(postHandler.selectPostProcessor(post) is GfycatPostProcessor)
    }

    @Test
    fun testPostControllerSelector_RedGifs() {
        val requestUrl = "https://www.reddit.com/r/Eyebleach/comments/gzn5on/\""
        val mockRequestHelper = Mockito.mock(RequestHelper::class.java)
        Mockito
            .`when`(mockRequestHelper.readHttpTextResponse("$requestUrl.json"))
            .thenReturn(
                createMockPostJsonResponse(
                    createMockPostObject(
                        mapOf(
                            "url" to "https://redgifs.com/somefancymocks.mp4"
                        )
                    )
                )
            )
        val postHandler = RedditPostHandler(mockRequestHelper)
        val post = postHandler.getRedditPostObj(requestUrl)
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
        val arr = JSONArray("[{\"data\": {\"children\": []}}]")
        arr.getJSONObject(0)
            .getJSONObject("data")
            .getJSONArray("children")
            .put(post)
        return arr.toString()
    }
}

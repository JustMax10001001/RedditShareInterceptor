package com.justsoft.redditshareinterceptor

import com.justsoft.redditshareinterceptor.processors.GfycatPostProcessor
import com.justsoft.redditshareinterceptor.processors.ImgurImageProcessor
import com.justsoft.redditshareinterceptor.processors.RedditImagePostProcessor
import com.justsoft.redditshareinterceptor.processors.RedditVideoPostProcessor
import com.justsoft.redditshareinterceptor.util.TestRequestHelper
import org.junit.Test

import org.junit.Assert.*

class RedditPostHandlerTest {

    private val redditPostHandler: RedditPostHandler = RedditPostHandler(TestRequestHelper())

    @Test
    fun extractSimpleUrl() {
        val testUrl = "https://www.reddit.com/r/nextfuckinglevel/comments/iloqpd/beautiful_horse_mural/"
        val expected = "https://www.reddit.com/r/nextfuckinglevel/comments/iloqpd/"
        assertEquals(expected, redditPostHandler.extractSimpleUrl(testUrl))
    }

    @Test
    fun testPostObjectDownload() {
        val post = redditPostHandler.getRedditPostObj("https://www.reddit.com/r/feedthememes/comments/ilt7jo/")
        assertEquals(post.title, "Let's not forget the original meme mod")
    }

    @Test
    fun testPostControllerSelector_RedditImage() {
        val post = redditPostHandler.getRedditPostObj("https://www.reddit.com/r/techsupportgore/comments/ilrwy8/")
        assert(redditPostHandler.selectPostProcessor(post) is RedditImagePostProcessor)
    }

    @Test
    fun testPostControllerSelector_ImgurImage() {
        val post = redditPostHandler.getRedditPostObj("https://www.reddit.com/r/feedthememes/comments/ilt7jo/")
        assert(redditPostHandler.selectPostProcessor(post) is ImgurImageProcessor)
    }

    @Test
    fun testPostControllerSelector_RedditVideo() {
        val post = redditPostHandler.getRedditPostObj("https://www.reddit.com/r/Unexpected/comments/ilrghf/")
        assert(redditPostHandler.selectPostProcessor(post) is RedditVideoPostProcessor)
    }

    @Test
    fun testPostControllerSelector_RedditGif() {
        val post = redditPostHandler.getRedditPostObj("https://www.reddit.com/r/aww/comments/gzhpze/")
        assert(redditPostHandler.selectPostProcessor(post) is RedditVideoPostProcessor)
    }

    @Test
    fun testPostControllerSelector_Gfycat() {
        val post = redditPostHandler.getRedditPostObj("https://www.reddit.com/r/Eyebleach/comments/gzn5on/")
        assert(redditPostHandler.selectPostProcessor(post) is GfycatPostProcessor)
    }
}
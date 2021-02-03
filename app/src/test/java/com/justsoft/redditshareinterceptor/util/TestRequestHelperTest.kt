package com.justsoft.redditshareinterceptor.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class TestRequestHelperTest {

    private val requestHelper = TestRequestHelper()

    @Before
    fun setUp() {
    }

    @Test
    @Ignore("Ripsave is no longer used")
    fun readHttpTextResponse() {
        assertNotEquals(
            "",
            requestHelper.readHttpTextResponse(
                "https://ripsave.com/getlink",
                mutableMapOf("url" to "https://www.reddit.com/r/aww/comments/gzhpze/")
            )
        )
    }

    @Test
    @Ignore("Ripsave is no longer used")
    fun readHttpJsonResponse() {
        assertNotNull(
            requestHelper.readHttpJsonResponse(
                "https://ripsave.com/getlink",
                mutableMapOf("url" to "https://www.reddit.com/r/aww/comments/gzhpze/")
            )
        )
    }

    @Test
    fun testContentLength() {
        assertEquals(
            8772805,
            requestHelper.getContentLength("https://thcf7.redgifs.com/PalatableFlashyBantamrooster-mobile.mp4")
        )
    }
}
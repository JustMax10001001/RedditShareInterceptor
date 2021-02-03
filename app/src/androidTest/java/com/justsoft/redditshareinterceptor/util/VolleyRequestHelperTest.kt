package com.justsoft.redditshareinterceptor.util

import androidx.test.platform.app.InstrumentationRegistry
import com.android.volley.toolbox.Volley
import com.justsoft.redditshareinterceptor.util.request.VolleyRequestHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class VolleyRequestHelperTest {

    private val volleyRequestHelper: VolleyRequestHelper by lazy {
        VolleyRequestHelper(
            Volley.newRequestQueue(InstrumentationRegistry.getInstrumentation().targetContext)
        )
    }

    @Test
    fun testContentLength() {
        assertEquals(2235703, volleyRequestHelper.getContentLength("https://thcf7.redgifs.com/WeirdTediousHound-mobile.mp4"))
    }
}
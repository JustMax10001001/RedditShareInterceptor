package com.justsoft.redditshareinterceptor

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.android.volley.toolbox.Volley
import com.justsoft.redditshareinterceptor.model.ContentType
import com.justsoft.redditshareinterceptor.util.VolleyRequestHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File


class RedditPostHandlerIntegrationTest {

    private lateinit var postHandler: RedditPostHandler

    @Before
    fun init() {
        postHandler = RedditPostHandler(
            VolleyRequestHelper(
                Volley.newRequestQueue(InstrumentationRegistry.getInstrumentation().targetContext)
            )
        )
        postHandler.error {
            throw it
        }
        postHandler.mediaSuccess { _, _ -> assert(true) }
    }

    @Test
    fun testMediaDownload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val targetFile = File(context.filesDir, "test.png")
        val targetFileUri = getInternalFileUri(context, targetFile)

        targetFile.delete()

        postHandler.downloadMedia(
            "https://i.imgur.com/GTrjBEI.png",
            openFileDescriptor(context, targetFileUri)
        )

        assert(targetFile.exists())
        assert(targetFile.length() > 0)
    }

    private fun openFileDescriptor(
        context: Context,
        targetFileUri: Uri
    ): ParcelFileDescriptor {
        return context.contentResolver.openFileDescriptor(
            targetFileUri,
            "w"
        )!!
    }


    private fun getInternalFileUri(
        context: Context,
        targetFile: File
    ): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.justsoft.redditshareinterceptor.provider",
            targetFile
        )!!
    }

    @Test
    fun postHandleTest_RedditVideo() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val targetFile = File(context.filesDir, "test.mp4")
        val targetFileUri = getInternalFileUri(context, targetFile)

        targetFile.delete()

        postHandler.handlePostUrl("https://www.reddit.com/r/okbuddyretard/comments/io5m2n/_/") {
            assertEquals(ContentType.VIDEO, it)
            openFileDescriptor(context, targetFileUri)
        }

        assert(targetFile.exists())
        assert(targetFile.length() > 0)
    }

    @Test
    fun postHandleTest_RedditImage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val targetFile = File(context.filesDir, "test.jpg")
        val targetFileUri = getInternalFileUri(context, targetFile)

        targetFile.delete()

        postHandler.handlePostUrl("https://www.reddit.com/r/techsupportgore/comments/ilrwy8/gaming_laptop_overheating_very_much_work_in/") {
            assertEquals(ContentType.IMAGE, it)
            openFileDescriptor(context, targetFileUri)
        }

        assert(targetFile.exists())
        assert(targetFile.length() > 0)
    }
}
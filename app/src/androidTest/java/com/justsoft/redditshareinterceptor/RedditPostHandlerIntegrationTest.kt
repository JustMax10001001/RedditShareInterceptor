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
        postHandler.mediaSuccess { _, _, _ -> assert(true) }
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

        postHandler.handlePostUrl("https://www.reddit.com/r/okbuddyretard/comments/io5m2n/_/") { contentType, _ ->
            assertEquals(ContentType.VIDEO, contentType)
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

        postHandler.handlePostUrl("https://www.reddit.com/r/techsupportgore/comments/ilrwy8/gaming_laptop_overheating_very_much_work_in/") { contentType, index ->
            assertEquals(ContentType.IMAGE, contentType)
            assertEquals(0, index)
            openFileDescriptor(context, targetFileUri)
        }

        assert(targetFile.exists())
        assert(targetFile.length() > 0)
    }

    @Test
    fun postHandleText_RedditGallery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val targetFiles = mutableListOf<File>()
        val targetFileUris = mutableListOf<Uri>()

        postHandler.handlePostUrl("https://www.reddit.com/r/announcements/comments/hrrh23/"){ contentType, index ->
            assertEquals(ContentType.GALLERY, contentType)

            targetFiles.add(File(context.filesDir, "test_$index.jpg"))
            targetFiles[index].delete()
            targetFileUris.add(getInternalFileUri(context, targetFiles[index]))

            openFileDescriptor(context, targetFileUris[index])
        }

        for (file in targetFiles) {
            assert(file.exists())
            assert(file.length() > 0)
        }
    }
}